package com.aegisauth.feature.authentication

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.data.datastore.AegisPreferences
import com.aegisauth.data.local.entity.AuthenticationEvent
import com.aegisauth.data.repository.AuthenticationHistoryRepository
import com.aegisauth.data.repository.BiometricTemplateRepository
import com.aegisauth.data.repository.SecurityAlertRepository
import com.aegisauth.core.audio.SoundFeedbackManager
import com.aegisauth.core.biometric.SystemBiometricManager
import com.aegisauth.core.security.AegisSessionManager
import com.aegisauth.core.security.EvidenceManager
import com.aegisauth.core.security.PasswordSecurityManager
import com.aegisauth.core.security.PatternSecurityManager
import com.aegisauth.domain.biohash.BioHashEngine
import com.aegisauth.domain.challenge.ChallengeEngine
import com.aegisauth.domain.embedding.FaceEmbeddingEngine
import com.aegisauth.domain.liveness.LivenessEngine
import com.aegisauth.domain.model.*
import com.aegisauth.domain.quality.ImageQualityEngine
import com.aegisauth.domain.risk.RiskEngine
import com.google.mlkit.vision.face.Face
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val qualityEngine: ImageQualityEngine,
    private val livenessEngine: LivenessEngine,
    private val riskEngine: RiskEngine,
    private val challengeEngine: ChallengeEngine,
    private val embeddingEngine: FaceEmbeddingEngine,
    private val bioHashEngine: BioHashEngine,
    private val biometricRepository: BiometricTemplateRepository,
    private val historyRepository: AuthenticationHistoryRepository,
    private val alertRepository: SecurityAlertRepository,
    private val evidenceManager: EvidenceManager,
    private val soundFeedbackManager: SoundFeedbackManager,
    val systemBiometricManager: SystemBiometricManager,
    val patternSecurityManager: PatternSecurityManager,
    val passwordSecurityManager: PasswordSecurityManager,
    private val aegisSessionManager: AegisSessionManager,
    private val preferences: AegisPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthenticationState>(AuthenticationState.FaceSearch)
    val authState = _authState.asStateFlow()

    private var targetPackageName: String = ""
    private var targetAppName: String = ""
    private var enrolledTemplates: List<BioHashTemplate> = emptyList()
    private var isVerifying = false
    private var activeChallengeJob: Job? = null
    private var currentChallengeState: ChallengeState? = null

    // M15.1/M15.2 — Track which stage of the fallback chain has genuinely failed
    private var faceFailed = false
    val isFaceAuthFailed: Boolean
        get() = faceFailed
    private var systemBiometricFailed = false

    init {
        livenessEngine.reset()
        challengeEngine.reset()
        preloadEnrolledTemplates()
    }

    private fun preloadEnrolledTemplates() {
        viewModelScope.launch {
            enrolledTemplates = biometricRepository.loadAllTemplates()
        }
    }

    fun setTargetApp(packageName: String, appName: String) {
        targetPackageName = packageName
        targetAppName = if (appName.isNotBlank()) appName else packageName
        checkTargetAppLockout()
    }

    private fun checkTargetAppLockout() {
        viewModelScope.launch {
            val recentFailed = historyRepository.getRecentFailedAttemptsCountForPackage(targetPackageName, windowMinutes = 10)
            val maxFailed = preferences.maxFailedAttempts.first()
            if (recentFailed >= maxFailed) {
                val lockoutMins = preferences.lockoutDurationMins.first()
                _authState.value = AuthenticationState.Locked(lockoutMins)
            }
        }
    }

    fun processFrame(face: Face?, frameWidth: Int, frameHeight: Int, faceBitmap: Bitmap?) {
        val currentState = _authState.value
        if (currentState is AuthenticationState.Authenticated ||
            currentState is AuthenticationState.Locked ||
            currentState is AuthenticationState.Failed ||
            currentState is AuthenticationState.SystemBiometricFailed ||
            currentState is AuthenticationState.RecoveryFallback ||
            currentState is AuthenticationState.AccessDenied ||
            isVerifying
        ) {
            return
        }

        // Active Challenge in progress?
        if (currentState is AuthenticationState.ActiveChallenge && currentChallengeState != null) {
            val evaluated = challengeEngine.evaluateFace(face, currentChallengeState!!)
            currentChallengeState = evaluated
            _authState.value = AuthenticationState.ActiveChallenge(evaluated)

            if (evaluated.isCompleted) {
                // Challenge passed, proceed to feature extraction & BioHash comparison
                activeChallengeJob?.cancel()
                if (face != null && faceBitmap != null) {
                    executeBiometricPipeline(face, faceBitmap, livenessScore = 0.95f, challengePassed = true)
                }
            } else if (evaluated.isFailed) {
                activeChallengeJob?.cancel()
                handleAuthenticationFailure("Active liveness challenge timed out or failed.")
            }
            return
        }

        if (face == null) {
            _authState.value = AuthenticationState.FaceSearch
            return
        }

        // 1. Evaluate Image Quality
        val quality = qualityEngine.evaluateQuality(face, frameWidth, frameHeight, faceBitmap)
        if (!quality.isAcceptable) {
            _authState.value = AuthenticationState.QualityCheck(quality.feedback, quality.overallScore)
            return
        }

        // 2. Evaluate Passive Liveness
        val liveness = livenessEngine.processFrame(face, quality)
        _authState.value = AuthenticationState.PassiveLiveness(liveness.score, liveness.signals)

        if (!liveness.isLive && liveness.confidence < 0.8f) {
            // Collecting more frames
            return
        }

        // 3. Evaluate Risk (Per-Package Isolation)
        viewModelScope.launch {
            val recentFailed = historyRepository.getRecentFailedAttemptsCountForPackage(targetPackageName, windowMinutes = 10)
            val maxFailed = preferences.maxFailedAttempts.first()
            if (recentFailed >= maxFailed) {
                val lockoutMins = preferences.lockoutDurationMins.first()
                _authState.value = AuthenticationState.Locked(lockoutMins)
                return@launch
            }

            val challengeMode = preferences.activeChallengeMode.first()
            val risk = riskEngine.evaluateRisk(liveness, quality, recentFailed, challengeMode)
            _authState.value = AuthenticationState.RiskEvaluation(risk)

            if (risk.requiresActiveChallenge && currentChallengeState == null) {
                triggerActiveChallenge()
            } else if (faceBitmap != null && !isVerifying) {
                executeBiometricPipeline(face, faceBitmap, liveness.score, challengePassed = false)
            }
        }
    }

    private fun triggerActiveChallenge() {
        soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.CHALLENGE_PROMPT)
        val initialChallenge = challengeEngine.startNewChallenge()
        currentChallengeState = initialChallenge
        _authState.value = AuthenticationState.ActiveChallenge(initialChallenge)

        activeChallengeJob?.cancel()
        activeChallengeJob = viewModelScope.launch {
            while (currentChallengeState != null && !currentChallengeState!!.isCompleted && !currentChallengeState!!.isFailed) {
                delay(1000)
                currentChallengeState = challengeEngine.tickTimer(currentChallengeState!!)
                _authState.value = AuthenticationState.ActiveChallenge(currentChallengeState!!)
                if (currentChallengeState!!.isFailed) {
                    handleAuthenticationFailure("Active challenge expired.")
                    break
                }
            }
        }
    }

    private fun executeBiometricPipeline(
        face: Face,
        faceBitmap: Bitmap,
        livenessScore: Float,
        challengePassed: Boolean
    ) {
        if (isVerifying) return
        isVerifying = true
        soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.VERIFICATION_STARTED)
        _authState.value = AuthenticationState.BioHashGeneration

        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Ensure templates are loaded
                if (enrolledTemplates.isEmpty()) {
                    enrolledTemplates = biometricRepository.loadAllTemplates()
                }

                if (enrolledTemplates.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        handleAuthenticationFailure("No enrolled biometric template found.", authMethod = "FACE")
                    }
                    return@launch
                }

                // 1. Generate on-device embedding
                val embedding = embeddingEngine.generateEmbedding(faceBitmap, face)

                // 2. Generate BioHash representation
                val probeBioHash = bioHashEngine.generateBioHash(embedding)

                _authState.value = AuthenticationState.Comparison(0.8f)

                // 3. Multi-template comparison in protected BioHash domain using threshold
                val threshold = preferences.similarityThreshold.first()
                val matchResult = bioHashEngine.compareBioHashes(enrolledTemplates, probeBioHash, threshold)

                withContext(Dispatchers.Main) {
                    if (matchResult.isMatch) {
                        // Success!
                        soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_SUCCESS)
                        if (targetPackageName == "com.aegisauth") {
                            aegisSessionManager.authenticateSession()
                        }
                        _authState.value = AuthenticationState.Authenticated(
                            appName = targetAppName.ifBlank { "Protected Application" },
                            similarity = matchResult.similarityScore,
                            liveness = livenessScore
                        )

                        // Record success event in Room
                        recordEvent(
                            authMethod = "FACE",
                            result = "SUCCESS",
                            riskLevel = if (challengePassed) "MEDIUM" else "LOW",
                            livenessScore = livenessScore,
                            qualityScore = 0.90f,
                            similarityScore = matchResult.similarityScore,
                            challengeRequired = challengePassed,
                            challengeResult = if (challengePassed) "PASSED" else null,
                            failureReason = null,
                            evidencePath = null
                        )
                    } else {
                        // Capture encrypted evidence frame for unauthorized attempt
                        val evidencePath = evidenceManager.captureEvidence(faceBitmap, targetPackageName)
                        if (evidencePath != null) {
                            soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.EVIDENCE_CAPTURED)
                        }
                        handleAuthenticationFailure(
                            reason = "Face verification mismatch (Similarity: ${(matchResult.similarityScore * 100).toInt()}%).",
                            evidencePath = evidencePath,
                            authMethod = "FACE"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    handleAuthenticationFailure("Biometric error: ${e.localizedMessage}", authMethod = "FACE")
                }
            } finally {
                isVerifying = false
            }
        }
    }

    fun authenticateWithSystemBiometric(activity: FragmentActivity) {
        if (!systemBiometricManager.isSystemBiometricAvailable(activity)) {
            return
        }
        val label = systemBiometricManager.getUiActionLabel(activity)
        systemBiometricManager.authenticate(
            activity = activity,
            title = "Biometric Verification",
            subtitle = "Securing $targetAppName",
            negativeButtonText = "Cancel",
            onResult = { result ->
                when (result) {
                    is SystemBiometricManager.BiometricPromptResult.Success -> {
                        soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_SUCCESS)
                        if (targetPackageName == "com.aegisauth") {
                            aegisSessionManager.authenticateSession()
                        }
                        _authState.value = AuthenticationState.Authenticated(
                            appName = targetAppName.ifBlank { "Protected Application" },
                            similarity = 1.0f,
                            liveness = 1.0f
                        )
                        viewModelScope.launch {
                            recordEvent(
                                authMethod = SystemBiometricManager.AUTH_METHOD_SYSTEM_BIOMETRIC,
                                result = "SUCCESS",
                                riskLevel = "LOW",
                                livenessScore = 1.0f,
                                qualityScore = 1.0f,
                                similarityScore = 1.0f,
                                challengeRequired = false,
                                challengeResult = null,
                                failureReason = null
                            )
                        }
                    }
                    is SystemBiometricManager.BiometricPromptResult.Failed -> {
                        handleAuthenticationFailure(
                            reason = "System biometric unrecognized.",
                            authMethod = SystemBiometricManager.AUTH_METHOD_SYSTEM_BIOMETRIC
                        )
                    }
                    is SystemBiometricManager.BiometricPromptResult.TemporaryLockout -> {
                        handleAuthenticationFailure(
                            reason = "System biometric temporarily locked due to too many attempts.",
                            authMethod = SystemBiometricManager.AUTH_METHOD_SYSTEM_BIOMETRIC
                        )
                    }
                    is SystemBiometricManager.BiometricPromptResult.PermanentLockout -> {
                        handleAuthenticationFailure(
                            reason = "System biometric permanently locked. Use device screen lock / recovery.",
                            authMethod = SystemBiometricManager.AUTH_METHOD_SYSTEM_BIOMETRIC
                        )
                    }
                    is SystemBiometricManager.BiometricPromptResult.UserCanceled -> {
                        // User cancelled system prompt, return to normal auth state
                    }
                    is SystemBiometricManager.BiometricPromptResult.Error -> {
                        handleAuthenticationFailure(
                            reason = "System biometric error: ${result.errString}",
                            authMethod = SystemBiometricManager.AUTH_METHOD_SYSTEM_BIOMETRIC
                        )
                    }
                }
            }
        )
    }

    fun verifyRecoveryPattern(points: List<Int>): Boolean {
        val isValid = patternSecurityManager.verifyPattern(points)
        if (isValid) {
            soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_SUCCESS)
            if (targetPackageName == "com.aegisauth") {
                aegisSessionManager.authenticateSession()
            }
            _authState.value = AuthenticationState.Authenticated(
                appName = targetAppName.ifBlank { "Protected Application" },
                similarity = 1.0f,
                liveness = 1.0f
            )
            viewModelScope.launch {
                recordEvent(
                    authMethod = "PATTERN",
                    result = "SUCCESS",
                    riskLevel = "LOW",
                    livenessScore = 1.0f,
                    qualityScore = 1.0f,
                    similarityScore = 1.0f,
                    challengeRequired = false,
                    challengeResult = null,
                    failureReason = null
                )
            }
        } else {
            handleAuthenticationFailure(
                reason = "Incorrect backup recovery pattern.",
                authMethod = "PATTERN"
            )
        }
        return isValid
    }

    fun verifyRecoveryPassword(password: String): Boolean {
        val isValid = passwordSecurityManager.verifyPassword(password)
        if (isValid) {
            soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_SUCCESS)
            if (targetPackageName == "com.aegisauth") {
                aegisSessionManager.authenticateSession()
            }
            _authState.value = AuthenticationState.Authenticated(
                appName = targetAppName.ifBlank { "Protected Application" },
                similarity = 1.0f,
                liveness = 1.0f
            )
            viewModelScope.launch {
                recordEvent(
                    authMethod = "PASSWORD",
                    result = "SUCCESS",
                    riskLevel = "LOW",
                    livenessScore = 1.0f,
                    qualityScore = 1.0f,
                    similarityScore = 1.0f,
                    challengeRequired = false,
                    challengeResult = null,
                    failureReason = null
                )
            }
        } else {
            handleAuthenticationFailure(
                reason = "Incorrect backup recovery password.",
                authMethod = "PASSWORD"
            )
        }
        return isValid
    }

    private fun handleAuthenticationFailure(
        reason: String,
        evidencePath: String? = null,
        authMethod: String = "FACE"
    ) {
        viewModelScope.launch {
            val recentFailed = historyRepository.getRecentFailedAttemptsCountForPackage(targetPackageName, windowMinutes = 10) + 1
            val maxFailed = preferences.maxFailedAttempts.first()
            val remaining = (maxFailed - recentFailed).coerceAtLeast(0)

            recordEvent(
                authMethod = authMethod,
                result = "FAILURE",
                riskLevel = "HIGH",
                livenessScore = 0.35f,
                qualityScore = 0.60f,
                similarityScore = 0.40f,
                challengeRequired = currentChallengeState != null,
                challengeResult = if (currentChallengeState != null) "FAILED" else null,
                failureReason = reason,
                evidencePath = evidencePath
            )

            if (recentFailed >= maxFailed) {
                soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.LOCKOUT)
                alertRepository.createAlert(
                    severity = "CRITICAL",
                    type = "FAILED_ATTEMPTS",
                    title = "Multiple Failed Unlock Attempts",
                    description = "$recentFailed consecutive unauthorized biometric attempts on $targetAppName."
                )
                val lockoutMins = preferences.lockoutDurationMins.first()
                _authState.value = AuthenticationState.Locked(lockoutMins)
            } else {
                soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_FAILED)
                // M15.1: Set per-method state so UI can drive the correct fallback chain
                when (authMethod) {
                    SystemBiometricManager.AUTH_METHOD_SYSTEM_BIOMETRIC -> {
                        systemBiometricFailed = true
                        if (targetPackageName == "com.aegisauth") {
                            _authState.value = AuthenticationState.RecoveryFallback
                        } else {
                            _authState.value = AuthenticationState.AccessDenied(reason = reason)
                        }
                    }
                    "PATTERN", "PASSWORD" -> {
                        // Recovery credential failure → generic Failed (allow retry)
                        _authState.value = AuthenticationState.Failed(
                            reason = reason,
                            canRetry = remaining > 0,
                            remainingAttempts = remaining
                        )
                    }
                    else -> {
                        // FACE pipeline failure
                        faceFailed = true
                        _authState.value = AuthenticationState.Failed(
                            reason = reason,
                            canRetry = remaining > 0,
                            remainingAttempts = remaining
                        )
                    }
                }
            }
        }
    }

    private suspend fun recordEvent(
        authMethod: String = "FACE",
        result: String,
        riskLevel: String,
        livenessScore: Float,
        qualityScore: Float,
        similarityScore: Float,
        challengeRequired: Boolean,
        challengeResult: String?,
        failureReason: String?,
        evidencePath: String? = null
    ) {
        historyRepository.recordEvent(
            AuthenticationEvent(
                packageName = targetPackageName,
                appName = targetAppName,
                authMethod = authMethod,
                result = result,
                riskLevel = riskLevel,
                livenessScore = livenessScore,
                qualityScore = qualityScore,
                similarityScore = similarityScore,
                challengeRequired = challengeRequired,
                challengeResult = challengeResult,
                failureReason = failureReason,
                evidencePath = evidencePath
            )
        )
    }

    fun retryAuthentication() {
        livenessEngine.reset()
        challengeEngine.reset()
        currentChallengeState = null
        isVerifying = false
        faceFailed = false
        systemBiometricFailed = false
        _authState.value = AuthenticationState.FaceSearch
    }
}
