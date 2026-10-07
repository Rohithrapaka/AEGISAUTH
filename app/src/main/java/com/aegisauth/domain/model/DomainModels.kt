package com.aegisauth.domain.model

import android.graphics.Rect

/**
 * Image quality evaluation scores.
 */
data class QualityResult(
    val overallScore: Float, // 0.0 to 1.0
    val blurScore: Float, // Higher is sharper
    val brightnessScore: Float, // 0.0 (dark) to 1.0 (overexposed), ideal ~0.5-0.7
    val contrastScore: Float,
    val poseScore: Float, // 1.0 is facing directly forward
    val faceSizeScore: Float, // Ratio of face bounding box to frame
    val isAcceptable: Boolean,
    val feedback: QualityFeedback
)

enum class QualityFeedback {
    GOOD,
    NO_FACE_DETECTED,
    FACE_TOO_SMALL,
    FACE_TOO_FAR,
    FACE_TOO_CLOSE,
    IMAGE_BLURRY,
    TOO_DARK,
    TOO_BRIGHT,
    EXTREME_ANGLE,
    HOLD_STILL
}

/**
 * Passive liveness evaluation metrics across multiple frames.
 */
data class LivenessSignals(
    val temporalConsistency: Float, // Frame-to-frame stability
    val landmarkMovement: Float, // Natural micro-movements (not static printed photo)
    val eyeBlinkScore: Float, // Eye aspect ratio dynamic variance
    val poseStability: Float, // Head pose consistency within 3D bounding volume
    val textureVariance: Float // High-frequency skin texture variance (spoof screen vs live skin)
)

data class LivenessResult(
    val score: Float, // 0.0 (spoof) to 1.0 (live)
    val confidence: Float,
    val signals: LivenessSignals,
    val isLive: Boolean,
    val statusMessage: String
)

/**
 * Multi-factor risk engine evaluation.
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class RiskResult(
    val level: RiskLevel,
    val riskScore: Float, // 0.0 (safest) to 1.0 (highest risk)
    val reasons: List<String>,
    val requiresActiveChallenge: Boolean
)

/**
 * Randomized active challenges.
 */
enum class ChallengeType(val prompt: String, val instruction: String) {
    BLINK("Blink your eyes", "Close and open both eyes naturally"),
    TURN_LEFT("Turn head left", "Slowly rotate your head to the left"),
    TURN_RIGHT("Turn head right", "Slowly rotate your head to the right"),
    LOOK_UP("Tilt head up", "Gently tilt your head upwards"),
    LOOK_DOWN("Tilt head down", "Gently tilt your head downwards"),
    SMILE("Smile gently", "Show a natural smile to the camera")
}

data class ChallengeState(
    val challenge: ChallengeType,
    val progress: Float = 0f, // 0.0 to 1.0
    val remainingSeconds: Int = 8,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)

/**
 * Protected BioHash representation.
 */
data class BioHashTemplate(
    val id: String,
    val version: Int,
    val bioHashBits: BooleanArray, // Quantized binary representation
    val featureLength: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BioHashTemplate
        if (id != other.id) return false
        if (!bioHashBits.contentEquals(other.bioHashBits)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + bioHashBits.contentHashCode()
        return result
    }
}

/**
 * Biometric matching result.
 */
data class MatchingResult(
    val isMatch: Boolean,
    val similarityScore: Float, // 0.0 to 1.0 (1.0 = identical)
    val hammingDistance: Int,
    val thresholdUsed: Float
)

/**
 * Formal Authentication State Machine.
 */
sealed class AuthenticationState {
    object Idle : AuthenticationState()
    object Initializing : AuthenticationState()
    object FaceSearch : AuthenticationState()
    data class FaceDetected(val bounds: Rect, val quality: QualityResult) : AuthenticationState()
    data class QualityCheck(val feedback: QualityFeedback, val score: Float) : AuthenticationState()
    data class PassiveLiveness(val score: Float, val signals: LivenessSignals) : AuthenticationState()
    data class RiskEvaluation(val risk: RiskResult) : AuthenticationState()
    data class ActiveChallenge(val challengeState: ChallengeState) : AuthenticationState()
    object BioHashGeneration : AuthenticationState()
    data class Comparison(val progress: Float) : AuthenticationState()
    data class Authenticated(val appName: String, val similarity: Float, val liveness: Float) : AuthenticationState()
    data class Failed(val reason: String, val canRetry: Boolean, val remainingAttempts: Int) : AuthenticationState()
    data class Locked(val remainingLockoutMinutes: Int) : AuthenticationState()

    // M15.1 — Explicit fallback chain states
    // SystemBiometricFallback: Face pipeline failed; system biometric prompt was shown
    object SystemBiometricFallback : AuthenticationState()
    // SystemBiometricFailed: System biometric prompt was completed and was rejected
    data class SystemBiometricFailed(val reason: String) : AuthenticationState()
    // RecoveryFallback: Full biometric chain failed for com.aegisauth; recovery options may be shown
    object RecoveryFallback : AuthenticationState()
    // AccessDenied: Full biometric chain failed for third-party; no recovery, no retry
    data class AccessDenied(val reason: String) : AuthenticationState()
}

/**
 * Multi-sample enrollment quality assessment metrics.
 */
data class EnrollmentMetrics(
    val captureQuality: Int, // e.g. 94% (average frame sharpness, contrast, composition)
    val faceConsistency: Int, // e.g. 91% (pairwise cosine similarity between sample embeddings)
    val lightingQuality: Int, // e.g. 88% (exposure and luminance stability across samples)
    val poseConsistency: Int, // e.g. 96% (head stability across capture window)
    val overallConfidence: Int, // e.g. 93% (deterministic weighted assessment)
    val isAcceptable: Boolean
)

