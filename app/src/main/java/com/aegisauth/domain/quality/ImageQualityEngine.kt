package com.aegisauth.domain.quality

import android.graphics.Bitmap
import android.graphics.Rect
import com.aegisauth.domain.model.QualityFeedback
import com.aegisauth.domain.model.QualityResult
import com.google.mlkit.vision.face.Face
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Singleton
class ImageQualityEngine @Inject constructor() {

    companion object {
        private const val MIN_FACE_RATIO = 0.09f  // Minimum 9% of preview area
        private const val MAX_FACE_RATIO = 0.78f  // Maximum 78% of preview area
        private const val DEFAULT_MAX_EULER_YAW = 22.0f  // Degrees rotation left/right for front verification
        private const val DEFAULT_MAX_EULER_PITCH = 20.0f // Degrees rotation up/down for front verification
        private const val ANGLED_MAX_EULER_YAW = 45.0f   // Degrees rotation left/right for guided poses
        private const val ANGLED_MAX_EULER_PITCH = 38.0f // Degrees rotation up/down for guided poses
        private const val MAX_EULER_ROLL = 22.0f         // Degrees tilt
        private const val MIN_BRIGHTNESS = 0.12f         // 0-1
        private const val MAX_BRIGHTNESS = 0.92f         // 0-1
        private const val MIN_BLUR_SCORE = 0.28f
    }

    /**
     * Evaluates face frame quality metrics and produces a normalized QualityResult.
     * When [allowAngledPose] is true (during multi-pose enrollment), pitch and yaw bounds
     * are expanded so intentional head turns are not rejected as low quality.
     */
    fun evaluateQuality(
        face: Face?,
        frameWidth: Int,
        frameHeight: Int,
        faceBitmap: Bitmap?,
        allowAngledPose: Boolean = false
    ): QualityResult {
        if (face == null || frameWidth <= 0 || frameHeight <= 0) {
            return QualityResult(
                overallScore = 0f,
                blurScore = 0f,
                brightnessScore = 0f,
                contrastScore = 0f,
                poseScore = 0f,
                faceSizeScore = 0f,
                isAcceptable = false,
                feedback = QualityFeedback.NO_FACE_DETECTED
            )
        }

        val bounds = face.boundingBox
        val frameArea = (frameWidth * frameHeight).toFloat()
        val faceArea = max(0, bounds.width() * bounds.height()).toFloat()
        val faceSizeRatio = faceArea / frameArea

        // 1. Face Size Score
        val faceSizeScore: Float
        val sizeFeedback: QualityFeedback?
        when {
            faceSizeRatio < MIN_FACE_RATIO -> {
                faceSizeScore = (faceSizeRatio / MIN_FACE_RATIO).coerceIn(0f, 1f)
                sizeFeedback = QualityFeedback.FACE_TOO_FAR
            }
            faceSizeRatio > MAX_FACE_RATIO -> {
                faceSizeScore = (1f - (faceSizeRatio - MAX_FACE_RATIO) / 0.25f).coerceIn(0f, 1f)
                sizeFeedback = QualityFeedback.FACE_TOO_CLOSE
            }
            else -> {
                faceSizeScore = 1.0f
                sizeFeedback = null
            }
        }

        // 2. Head Pose Score (Euler X = pitch, Euler Y = yaw, Euler Z = roll)
        val maxYaw = if (allowAngledPose) ANGLED_MAX_EULER_YAW else DEFAULT_MAX_EULER_YAW
        val maxPitch = if (allowAngledPose) ANGLED_MAX_EULER_PITCH else DEFAULT_MAX_EULER_PITCH

        val yaw = abs(face.headEulerAngleY)
        val pitch = abs(face.headEulerAngleX)
        val roll = abs(face.headEulerAngleZ)

        val yawScore = (1f - (yaw / maxYaw)).coerceIn(0f, 1f)
        val pitchScore = (1f - (pitch / maxPitch)).coerceIn(0f, 1f)
        val rollScore = (1f - (roll / MAX_EULER_ROLL)).coerceIn(0f, 1f)
        val poseScore = (yawScore * 0.45f + pitchScore * 0.35f + rollScore * 0.20f)

        // 3. Brightness, Contrast & Sharpness (Blur) from Bitmap if available
        var brightnessScore = 0.70f
        var contrastScore = 0.75f
        var blurScore = 0.80f

        faceBitmap?.let { bmp ->
            val (bright, contrast, blur) = analyzePixelMetrics(bmp)
            brightnessScore = bright
            contrastScore = contrast
            blurScore = blur
        }

        // Determine overall score and primary user feedback
        val overallScore = (
            faceSizeScore * 0.30f +
            poseScore * 0.30f +
            blurScore * 0.20f +
            brightnessScore * 0.10f +
            contrastScore * 0.10f
        ).coerceIn(0f, 1f)

        val primaryFeedback: QualityFeedback = when {
            sizeFeedback != null -> sizeFeedback
            yaw > maxYaw || pitch > maxPitch -> QualityFeedback.EXTREME_ANGLE
            blurScore < MIN_BLUR_SCORE -> QualityFeedback.IMAGE_BLURRY
            brightnessScore < MIN_BRIGHTNESS -> QualityFeedback.TOO_DARK
            brightnessScore > MAX_BRIGHTNESS -> QualityFeedback.TOO_BRIGHT
            overallScore < 0.55f -> QualityFeedback.HOLD_STILL
            else -> QualityFeedback.GOOD
        }

        val isAcceptable = overallScore >= 0.55f &&
                faceSizeScore >= 0.60f &&
                poseScore >= 0.45f &&
                blurScore >= MIN_BLUR_SCORE

        return QualityResult(
            overallScore = overallScore,
            blurScore = blurScore,
            brightnessScore = brightnessScore,
            contrastScore = contrastScore,
            poseScore = poseScore,
            faceSizeScore = faceSizeScore,
            isAcceptable = isAcceptable,
            feedback = primaryFeedback
        )
    }

    /**
     * Performs fast luminance, contrast, and variance of Laplacian (sharpness) analysis on the face bitmap.
     */
    private fun analyzePixelMetrics(bitmap: Bitmap): Triple<Float, Float, Float> {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 2 || height <= 2) return Triple(0.5f, 0.5f, 0.5f)

        val sampleStep = max(1, min(width, height) / 48) // Sample a subgrid for high performance 60fps
        var totalLuminance = 0.0
        var totalSquaredLuminance = 0.0
        var laplacianVarianceSum = 0.0
        var samplesCount = 0

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in sampleStep until height - sampleStep step sampleStep) {
            for (x in sampleStep until width - sampleStep step sampleStep) {
                val index = y * width + x
                val pixel = pixels[index]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // Standard luminance formula (Rec. 601)
                val lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                totalLuminance += lum
                totalSquaredLuminance += lum * lum

                // Fast 4-connected discrete Laplacian filter for sharpness
                val pTop = pixels[(y - sampleStep) * width + x]
                val pBottom = pixels[(y + sampleStep) * width + x]
                val pLeft = pixels[y * width + (x - sampleStep)]
                val pRight = pixels[y * width + (x + sampleStep)]

                val lumTop = ((pTop shr 16 and 0xFF) * 0.299 + (pTop shr 8 and 0xFF) * 0.587 + (pTop and 0xFF) * 0.114) / 255.0
                val lumBottom = ((pBottom shr 16 and 0xFF) * 0.299 + (pBottom shr 8 and 0xFF) * 0.587 + (pBottom and 0xFF) * 0.114) / 255.0
                val lumLeft = ((pLeft shr 16 and 0xFF) * 0.299 + (pLeft shr 8 and 0xFF) * 0.587 + (pLeft and 0xFF) * 0.114) / 255.0
                val lumRight = ((pRight shr 16 and 0xFF) * 0.299 + (pRight shr 8 and 0xFF) * 0.587 + (pRight and 0xFF) * 0.114) / 255.0

                val laplacian = abs(4.0 * lum - (lumTop + lumBottom + lumLeft + lumRight))
                laplacianVarianceSum += laplacian
                samplesCount++
            }
        }

        if (samplesCount == 0) return Triple(0.5f, 0.5f, 0.5f)

        val meanLuminance = (totalLuminance / samplesCount).toFloat()
        val variance = ((totalSquaredLuminance / samplesCount) - (meanLuminance * meanLuminance)).coerceAtLeast(0.0)
        val contrast = (Math.sqrt(variance) * 3.0).toFloat().coerceIn(0f, 1f)

        // Normalize Laplacian edge sharpness (0 = blurred, > 0.05 is crisp)
        val meanLaplacian = (laplacianVarianceSum / samplesCount).toFloat()
        val sharpness = (meanLaplacian * 15f).coerceIn(0f, 1f)

        // Ideal brightness around 0.35 to 0.75
        val normalizedBrightness = when {
            meanLuminance < 0.2f -> (meanLuminance / 0.2f) * 0.5f
            meanLuminance > 0.85f -> (1f - (meanLuminance - 0.85f) / 0.15f) * 0.5f
            else -> 1.0f
        }.coerceIn(0f, 1f)

        return Triple(normalizedBrightness, contrast, sharpness)
    }
}
