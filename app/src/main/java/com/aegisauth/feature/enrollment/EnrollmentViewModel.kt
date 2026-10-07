package com.aegisauth.feature.enrollment

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.core.audio.SoundFeedbackManager
import com.aegisauth.data.repository.BiometricTemplateRepository
import com.aegisauth.domain.biohash.BioHashEngine
import com.aegisauth.domain.embedding.FaceEmbeddingEngine
import com.aegisauth.domain.model.BioHashTemplate
import com.aegisauth.domain.model.EnrollmentMetrics
import com.aegisauth.domain.model.QualityFeedback
import com.aegisauth.domain.model.QualityResult
import com.aegisauth.domain.quality.ImageQualityEngine
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

enum class EnrollmentPoseStep(
    val stepIndex: Int,
    val prompt: String,
    val targetDescription: String,
    val expectedPoseName: String
) {
    FRONT_1(1, "Look directly at the camera", "FRONT (1/3)", "FACE STRAIGHT"),
    FRONT_2(2, "Hold still facing front", "FRONT (2/3)", "FACE STRAIGHT"),
    FRONT_3(3, "Hold still facing front", "FRONT (3/3)", "FACE STRAIGHT"),
    TURN_LEFT(4, "Turn your head slightly LEFT", "TURN LEFT", "TURN LEFT"),
    TURN_RIGHT(5, "Turn your head slightly RIGHT", "TURN RIGHT", "TURN RIGHT"),
    LOOK_UP(6, "Look slightly UP", "LOOK UP", "LOOK UP"),
    LOOK_DOWN(7, "Look slightly DOWN", "LOOK DOWN", "LOOK DOWN")
}

data class EnrollmentDiagnosticState(
    val currentStep: EnrollmentPoseStep,
    val expectedPose: String,
    val detectedYaw: Float,
    val detectedPitch: Float,
    val detectedRoll: Float,
    val poseValid: Boolean,
    val qualityValid: Boolean,
    val stabilizationComplete: Boolean,
    val captureTriggered: Boolean,
    val captureCompleted: Boolean,
    val failureReason: String? = null
)

sealed class EnrollmentUiState {
    object Idle : EnrollmentUiState()
    data class Scanning(
        val sampleCount: Int,
        val maxSamples: Int,
        val quality: QualityResult,
        val progress: Float,
        val currentStep: EnrollmentPoseStep,
        val userInstruction: String = "Center your face inside the targeting reticle",
        val diagnostics: EnrollmentDiagnosticState
    ) : EnrollmentUiState()
    object Processing : EnrollmentUiState()
    data class Success(
        val template: BioHashTemplate,
        val metrics: EnrollmentMetrics
    ) : EnrollmentUiState()
    data class LowQuality(
        val metrics: EnrollmentMetrics,
        val message: String
    ) : EnrollmentUiState()
    data class Error(val message: String) : EnrollmentUiState()
}

data class SampleCaptureData(
    val embedding: FloatArray,
    val quality: QualityResult,
    val yaw: Float,
    val pitch: Float,
    val roll: Float,
    val step: EnrollmentPoseStep,
    val capturedAt: Long
)

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
    private val qualityEngine: ImageQualityEngine,
    private val embeddingEngine: FaceEmbeddingEngine,
    private val bioHashEngine: BioHashEngine,
    private val biometricRepository: BiometricTemplateRepository,
    private val soundFeedbackManager: SoundFeedbackManager
) : ViewModel() {

    companion object {
        const val REQUIRED_SAMPLES = 7
        private const val INTER_SAMPLE_COOLDOWN_MS = 600L
        private const val STABILIZATION_REQUIRED_MS = 250L
    }

    private val initialDiagnostics = EnrollmentDiagnosticState(
        currentStep = EnrollmentPoseStep.FRONT_1,
        expectedPose = "FACE STRAIGHT",
        detectedYaw = 0f,
        detectedPitch = 0f,
        detectedRoll = 0f,
        poseValid = false,
        qualityValid = false,
        stabilizationComplete = false,
        captureTriggered = false,
        captureCompleted = false
    )

    private val _uiState = MutableStateFlow<EnrollmentUiState>(
        EnrollmentUiState.Scanning(
            sampleCount = 1,
            maxSamples = REQUIRED_SAMPLES,
            quality = QualityResult(0f, 0f, 0f, 0f, 0f, 0f, false, QualityFeedback.NO_FACE_DETECTED),
            progress = 0f,
            currentStep = EnrollmentPoseStep.FRONT_1,
            userInstruction = "Center your face inside the targeting reticle",
            diagnostics = initialDiagnostics
        )
    )
    val uiState = _uiState.asStateFlow()

    private val collectedSamples = mutableListOf<SampleCaptureData>()
    private var isProcessingCapture = false
    private var lastCaptureTime = 0L
    private var poseStabilizationStartTime = 0L

    fun getCurrentTargetStep(): EnrollmentPoseStep {
        val count = collectedSamples.size
        return when (count) {
            0 -> EnrollmentPoseStep.FRONT_1
            1 -> EnrollmentPoseStep.FRONT_2
            2 -> EnrollmentPoseStep.FRONT_3
            3 -> EnrollmentPoseStep.TURN_LEFT
            4 -> EnrollmentPoseStep.TURN_RIGHT
            5 -> EnrollmentPoseStep.LOOK_UP
            6 -> EnrollmentPoseStep.LOOK_DOWN
            else -> EnrollmentPoseStep.FRONT_1
        }
    }

    fun isPoseSatisfied(targetStep: EnrollmentPoseStep, yaw: Float, pitch: Float, roll: Float = 0f): Boolean {
        if (abs(roll) > 20.0f) return false
        return when (targetStep) {
            EnrollmentPoseStep.FRONT_1,
            EnrollmentPoseStep.FRONT_2,
            EnrollmentPoseStep.FRONT_3 -> {
                abs(yaw) <= 9.0f && abs(pitch) <= 9.0f
            }
            EnrollmentPoseStep.TURN_LEFT -> {
                // Negative yaw in standard ML Kit front-camera coordinate convention
                (yaw in -32.0f..-6.0f) && abs(pitch) <= 15.0f
            }
            EnrollmentPoseStep.TURN_RIGHT -> {
                // Positive yaw in standard ML Kit front-camera coordinate convention
                (yaw in 6.0f..32.0f) && abs(pitch) <= 15.0f
            }
            EnrollmentPoseStep.LOOK_UP -> {
                // Positive pitch for chin up
                (pitch in 6.0f..28.0f) && abs(yaw) <= 15.0f
            }
            EnrollmentPoseStep.LOOK_DOWN -> {
                // Negative pitch for chin down
                (pitch in -28.0f..-6.0f) && abs(yaw) <= 15.0f
            }
        }
    }

    fun getSpecificPoseGuidance(targetStep: EnrollmentPoseStep, yaw: Float, pitch: Float): String {
        return when (targetStep) {
            EnrollmentPoseStep.FRONT_1,
            EnrollmentPoseStep.FRONT_2,
            EnrollmentPoseStep.FRONT_3 -> {
                if (abs(yaw) > 9.0f || abs(pitch) > 9.0f) "Look directly at the front camera" else "Hold position facing front"
            }
            EnrollmentPoseStep.TURN_LEFT -> {
                when {
                    yaw > -6.0f -> "Turn head slightly more to the LEFT"
                    yaw < -32.0f -> "Turned too far. Turn slightly back to center"
                    abs(pitch) > 15.0f -> "Keep head level (tilt less)"
                    else -> "Hold position turned LEFT"
                }
            }
            EnrollmentPoseStep.TURN_RIGHT -> {
                when {
                    yaw < 6.0f -> "Turn head slightly more to the RIGHT"
                    yaw > 32.0f -> "Turned too far. Turn slightly back to center"
                    abs(pitch) > 15.0f -> "Keep head level (tilt less)"
                    else -> "Hold position turned RIGHT"
                }
            }
            EnrollmentPoseStep.LOOK_UP -> {
                when {
                    pitch < 6.0f -> "Tilt head slightly UP"
                    pitch > 28.0f -> "Tilted too high. Tilt slightly back down"
                    abs(yaw) > 15.0f -> "Face forward and tilt UP"
                    else -> "Hold position looking UP"
                }
            }
            EnrollmentPoseStep.LOOK_DOWN -> {
                when {
                    pitch > -6.0f -> "Tilt head slightly DOWN"
                    pitch < -28.0f -> "Tilted too low. Tilt slightly back up"
                    abs(yaw) > 15.0f -> "Face forward and tilt DOWN"
                    else -> "Hold position looking DOWN"
                }
            }
        }
    }

    fun processFrame(face: Face?, frameWidth: Int, frameHeight: Int, faceBitmap: Bitmap?) {
        val currentState = _uiState.value
        if (currentState is EnrollmentUiState.Processing ||
            currentState is EnrollmentUiState.Success ||
            currentState is EnrollmentUiState.LowQuality
        ) {
            return
        }

        val completedCount = collectedSamples.size
        val activeSampleIndex = (completedCount + 1).coerceAtMost(REQUIRED_SAMPLES)
        val progress = completedCount.toFloat() / REQUIRED_SAMPLES
        val targetStep = getCurrentTargetStep()

        val currentTime = System.currentTimeMillis()
        val inCooldown = (currentTime - lastCaptureTime) < INTER_SAMPLE_COOLDOWN_MS

        val yaw = face?.headEulerAngleY ?: 0f
        val pitch = face?.headEulerAngleX ?: 0f
        val roll = face?.headEulerAngleZ ?: 0f

        val isAngledPose = targetStep != EnrollmentPoseStep.FRONT_1 &&
                targetStep != EnrollmentPoseStep.FRONT_2 &&
                targetStep != EnrollmentPoseStep.FRONT_3

        val quality = qualityEngine.evaluateQuality(
            face = face,
            frameWidth = frameWidth,
            frameHeight = frameHeight,
            faceBitmap = faceBitmap,
            allowAngledPose = isAngledPose
        )

        val poseSatisfied = face != null && isPoseSatisfied(targetStep, yaw, pitch, roll)
        val qualityValid = quality.isAcceptable

        // Stabilization tracking
        val stabilizationComplete: Boolean
        if (face != null && poseSatisfied && qualityValid && !inCooldown && !isProcessingCapture) {
            if (poseStabilizationStartTime == 0L) {
                poseStabilizationStartTime = currentTime
            }
            val elapsedStabilization = currentTime - poseStabilizationStartTime
            stabilizationComplete = elapsedStabilization >= STABILIZATION_REQUIRED_MS
        } else {
            poseStabilizationStartTime = 0L
            stabilizationComplete = false
        }

        val dynamicInstruction = when {
            face == null -> "Center your face inside the targeting reticle"
            inCooldown -> "Sample $completedCount of $REQUIRED_SAMPLES registered. Get ready..."
            !qualityValid && quality.feedback == QualityFeedback.TOO_DARK -> "Move to better lighting"
            !qualityValid && quality.feedback == QualityFeedback.TOO_BRIGHT -> "Avoid harsh backlight"
            !qualityValid && quality.feedback == QualityFeedback.IMAGE_BLURRY -> "Hold camera steady"
            !qualityValid && (quality.feedback == QualityFeedback.FACE_TOO_FAR || quality.feedback == QualityFeedback.FACE_TOO_SMALL) -> "Move slightly closer"
            !qualityValid && quality.feedback == QualityFeedback.FACE_TOO_CLOSE -> "Move slightly back"
            !poseSatisfied -> getSpecificPoseGuidance(targetStep, yaw, pitch)
            stabilizationComplete -> "Capturing sample $activeSampleIndex..."
            else -> "Ideal angle! Hold still..."
        }

        val diagnostics = EnrollmentDiagnosticState(
            currentStep = targetStep,
            expectedPose = targetStep.expectedPoseName,
            detectedYaw = yaw,
            detectedPitch = pitch,
            detectedRoll = roll,
            poseValid = poseSatisfied,
            qualityValid = qualityValid,
            stabilizationComplete = stabilizationComplete,
            captureTriggered = isProcessingCapture,
            captureCompleted = false,
            failureReason = if (!qualityValid) quality.feedback.name else if (!poseSatisfied) "POSE_NOT_SATISFIED" else null
        )

        _uiState.value = EnrollmentUiState.Scanning(
            sampleCount = activeSampleIndex,
            maxSamples = REQUIRED_SAMPLES,
            quality = quality,
            progress = progress,
            currentStep = targetStep,
            userInstruction = dynamicInstruction,
            diagnostics = diagnostics
        )

        // Trigger capture when stabilized
        val isValidForCapture = face != null &&
                faceBitmap != null &&
                qualityValid &&
                poseSatisfied &&
                stabilizationComplete &&
                !inCooldown &&
                !isProcessingCapture &&
                completedCount < REQUIRED_SAMPLES

        if (isValidForCapture) {
            isProcessingCapture = true
            lastCaptureTime = currentTime
            poseStabilizationStartTime = 0L

            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val embedding = embeddingEngine.generateEmbedding(faceBitmap, face)
                    val sample = SampleCaptureData(
                        embedding = embedding,
                        quality = quality,
                        yaw = yaw,
                        pitch = pitch,
                        roll = roll,
                        step = targetStep,
                        capturedAt = currentTime
                    )
                    collectedSamples.add(sample)
                    soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.VERIFICATION_STARTED)

                    val newCount = collectedSamples.size
                    val newProgress = newCount.toFloat() / REQUIRED_SAMPLES
                    val nextStep = getCurrentTargetStep()
                    val nextActiveIndex = (newCount + 1).coerceAtMost(REQUIRED_SAMPLES)

                    val postCaptureDiagnostics = diagnostics.copy(
                        currentStep = nextStep,
                        expectedPose = nextStep.expectedPoseName,
                        captureTriggered = false,
                        captureCompleted = true
                    )

                    _uiState.value = EnrollmentUiState.Scanning(
                        sampleCount = nextActiveIndex,
                        maxSamples = REQUIRED_SAMPLES,
                        quality = quality,
                        progress = newProgress,
                        currentStep = nextStep,
                        userInstruction = if (newCount < REQUIRED_SAMPLES) "Sample $newCount of $REQUIRED_SAMPLES registered!" else "Processing enrollment...",
                        diagnostics = postCaptureDiagnostics
                    )

                    if (newCount >= REQUIRED_SAMPLES) {
                        finalizeEnrollment()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessingCapture = false
                }
            }
        }
    }

    private suspend fun finalizeEnrollment() = withContext(Dispatchers.Default) {
        _uiState.value = EnrollmentUiState.Processing
        try {
            if (collectedSamples.size < REQUIRED_SAMPLES) {
                soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_FAILED)
                _uiState.value = EnrollmentUiState.Error("Insufficient samples captured.")
                return@withContext
            }

            // 1. Calculate genuine Enrollment Metrics from collected samples
            val metrics = computeEnrollmentMetrics(collectedSamples)

            // Check if enrollment quality satisfies minimum consistency thresholds
            if (!metrics.isAcceptable) {
                soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_FAILED)
                _uiState.value = EnrollmentUiState.LowQuality(
                    metrics = metrics,
                    message = "Enrollment quality is low. Pose samples were inconsistent. Please try again with clear lighting and steady positioning."
                )
                return@withContext
            }

            // 2. Aggregate collected 7 sample embeddings into robust normalized centroid vector
            val dim = collectedSamples.first().embedding.size
            val avgVector = FloatArray(dim)

            for (sample in collectedSamples) {
                for (i in 0 until dim) {
                    avgVector[i] += sample.embedding[i]
                }
            }
            for (i in 0 until dim) {
                avgVector[i] /= collectedSamples.size.toFloat()
            }

            // 3. L2 normalize average centroid vector
            var sumSq = 0f
            for (v in avgVector) sumSq += v * v
            val norm = sqrt(sumSq).coerceAtLeast(1e-6f)
            for (i in 0 until dim) avgVector[i] /= norm

            // 4. Generate BioHash template from aggregated representation
            val bioHashTemplate = bioHashEngine.generateBioHash(avgVector)

            // 5. Securely persist encrypted template in Keystore-backed storage
            val saved = biometricRepository.saveTemplate(bioHashTemplate)
            if (saved) {
                soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_SUCCESS)
                _uiState.value = EnrollmentUiState.Success(
                    template = bioHashTemplate,
                    metrics = metrics
                )
            } else {
                soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_FAILED)
                _uiState.value = EnrollmentUiState.Error("Failed to store encrypted template in secure storage.")
            }
        } catch (e: Exception) {
            soundFeedbackManager.playSound(SoundFeedbackManager.SoundEvent.AUTH_FAILED)
            _uiState.value = EnrollmentUiState.Error("Error generating biometric template: ${e.localizedMessage}")
        }
    }

    /**
     * Computes genuine multi-sample quality and consistency assessment.
     */
    private fun computeEnrollmentMetrics(samples: List<SampleCaptureData>): EnrollmentMetrics {
        val n = samples.size

        // A. Capture Quality: Average frame quality score across 7 samples
        val avgQualityScore = samples.map { it.quality.overallScore }.average().toFloat()
        val captureQualityPct = (avgQualityScore * 100f).toInt().coerceIn(0, 100)

        // B. Face Consistency: Pairwise cosine similarity between all distinct pairs of normalized embeddings
        var totalPairSimilarity = 0f
        var pairCount = 0
        for (i in 0 until n) {
            for (j in (i + 1) until n) {
                var dot = 0f
                val v1 = samples[i].embedding
                val v2 = samples[j].embedding
                for (k in v1.indices) {
                    dot += v1[k] * v2[k]
                }
                totalPairSimilarity += dot
                pairCount++
            }
        }
        val avgCosineSim = if (pairCount > 0) totalPairSimilarity / pairCount else 0.85f
        val faceConsistencyPct = (avgCosineSim * 100f).toInt().coerceIn(0, 100)

        // C. Lighting Quality: Average brightness and contrast balance
        val avgBrightness = samples.map { it.quality.brightnessScore }.average().toFloat()
        val avgContrast = samples.map { it.quality.contrastScore }.average().toFloat()
        val lightingScore = (avgBrightness * 0.6f + avgContrast * 0.4f)
        val lightingQualityPct = (lightingScore * 100f).toInt().coerceIn(0, 100)

        // D. Pose Coverage: Confirms distinct multi-angle samples were successfully registered
        val distinctSteps = samples.map { it.step }.distinct().size
        val poseCoverageScore = (distinctSteps.toFloat() / REQUIRED_SAMPLES.toFloat()).coerceIn(0.5f, 1.0f)
        val poseConsistencyPct = (poseCoverageScore * 100f).toInt().coerceIn(0, 100)

        // E. Overall Confidence: Deterministic weighted combination
        val overallScore = (
            captureQualityPct * 0.30f +
            faceConsistencyPct * 0.40f +
            lightingQualityPct * 0.15f +
            poseConsistencyPct * 0.15f
        ).toInt().coerceIn(0, 100)

        val isAcceptable = overallScore >= 60 && faceConsistencyPct >= 55

        return EnrollmentMetrics(
            captureQuality = captureQualityPct,
            faceConsistency = faceConsistencyPct,
            lightingQuality = lightingQualityPct,
            poseConsistency = poseConsistencyPct,
            overallConfidence = overallScore,
            isAcceptable = isAcceptable
        )
    }

    fun retryEnrollment() {
        collectedSamples.clear()
        isProcessingCapture = false
        lastCaptureTime = 0L
        poseStabilizationStartTime = 0L
        _uiState.value = EnrollmentUiState.Scanning(
            sampleCount = 1,
            maxSamples = REQUIRED_SAMPLES,
            quality = QualityResult(0f, 0f, 0f, 0f, 0f, 0f, false, QualityFeedback.NO_FACE_DETECTED),
            progress = 0f,
            currentStep = EnrollmentPoseStep.FRONT_1,
            userInstruction = "Center your face inside the targeting reticle",
            diagnostics = initialDiagnostics
        )
    }
}

