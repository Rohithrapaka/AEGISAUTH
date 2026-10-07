package com.aegisauth.domain.model

/**
 * M15.2 Standardized authentication failure reason taxonomy.
 * Provides machine-readable codes, user-facing titles, concise explanations,
 * and deterministic mappings from low-level signals (quality, liveness, challenge, BioHash).
 */
enum class AuthenticationFailureReason(
    val code: String,
    val title: String,
    val explanation: String
) {
    NO_FACE_DETECTED(
        code = "NO_FACE_DETECTED",
        title = "NO FACE DETECTED",
        explanation = "No face visible in camera preview."
    ),
    FACE_TOO_FAR(
        code = "FACE_TOO_FAR",
        title = "FACE TOO FAR",
        explanation = "Face area is too small relative to frame."
    ),
    FACE_TOO_CLOSE(
        code = "FACE_TOO_CLOSE",
        title = "FACE TOO CLOSE",
        explanation = "Face is too close to camera lens."
    ),
    FACE_NOT_CLEAR(
        code = "FACE_NOT_CLEAR",
        title = "FACE NOT CLEAR",
        explanation = "Face image lacks focus or sufficient sharpness."
    ),
    POOR_LIGHTING(
        code = "POOR_LIGHTING",
        title = "POOR LIGHTING",
        explanation = "Ambient lighting is too dark or overexposed."
    ),
    FACE_ANGLE_INVALID(
        code = "FACE_ANGLE_INVALID",
        title = "FACE ANGLE NOT ACCEPTABLE",
        explanation = "Head pose rotation angle exceeds front threshold."
    ),
    FACE_QUALITY_TOO_LOW(
        code = "FACE_QUALITY_TOO_LOW",
        title = "FACE QUALITY TOO LOW",
        explanation = "Composite quality score did not meet acceptance threshold."
    ),
    PASSIVE_LIVENESS_FAILED(
        code = "PASSIVE_LIVENESS_FAILED",
        title = "PASSIVE LIVENESS FAILED",
        explanation = "Micro-movement or texture analysis failed liveness verification."
    ),
    ACTIVE_CHALLENGE_FAILED(
        code = "ACTIVE_CHALLENGE_FAILED",
        title = "ACTIVE CHALLENGE FAILED",
        explanation = "Dynamic motion challenge was not completed within timeout."
    ),
    FACE_DOES_NOT_MATCH(
        code = "FACE_DOES_NOT_MATCH",
        title = "FACE DOES NOT MATCH",
        explanation = "Biometric probe template does not match enrolled BioHash."
    ),
    SYSTEM_BIOMETRIC_FAILED(
        code = "SYSTEM_BIOMETRIC_FAILED",
        title = "DEVICE BIOMETRIC FAILED",
        explanation = "Platform fingerprint or system biometric was rejected."
    ),
    RECOVERY_FAILED(
        code = "RECOVERY_FAILED",
        title = "RECOVERY CREDENTIAL FAILED",
        explanation = "Backup password or recovery pattern was incorrect."
    ),
    AUTH_TIMEOUT(
        code = "AUTH_TIMEOUT",
        title = "AUTHENTICATION TIMED OUT",
        explanation = "Authentication window expired before verification finished."
    ),
    ACCESS_DENIED(
        code = "ACCESS_DENIED",
        title = "ACCESS DENIED",
        explanation = "Authentication failed and no further fallback is permitted."
    );

    companion object {
        fun fromQualityFeedback(feedback: QualityFeedback): AuthenticationFailureReason {
            return when (feedback) {
                QualityFeedback.NO_FACE_DETECTED -> NO_FACE_DETECTED
                QualityFeedback.FACE_TOO_SMALL,
                QualityFeedback.FACE_TOO_FAR -> FACE_TOO_FAR
                QualityFeedback.FACE_TOO_CLOSE -> FACE_TOO_CLOSE
                QualityFeedback.IMAGE_BLURRY -> FACE_NOT_CLEAR
                QualityFeedback.TOO_DARK,
                QualityFeedback.TOO_BRIGHT -> POOR_LIGHTING
                QualityFeedback.EXTREME_ANGLE -> FACE_ANGLE_INVALID
                else -> FACE_QUALITY_TOO_LOW
            }
        }
    }
}
