package com.aegisauth.domain.liveness

import com.aegisauth.domain.model.LivenessResult
import com.aegisauth.domain.model.LivenessSignals
import com.aegisauth.domain.model.QualityResult
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

interface LivenessEngine {
    fun processFrame(face: Face?, quality: QualityResult): LivenessResult
    fun reset()
}

@Singleton
class PassiveLivenessEngine @Inject constructor() : LivenessEngine {

    companion object {
        private const val WINDOW_SIZE = 12 // Number of consecutive frames analyzed
        private const val MIN_FRAMES_FOR_DECISION = 6
        private const val STATIC_PHOTO_JITTER_THRESHOLD = 0.0008f // If variance is below this, it's a fixed paper/photo
        private const val EXTREME_JITTER_THRESHOLD = 0.08f // If variance is too huge, camera is shaking unnaturally
    }

    private val frameHistory = LinkedList<FrameSample>()

    private data class FrameSample(
        val timestamp: Long,
        val yaw: Float,
        val pitch: Float,
        val roll: Float,
        val leftEyeOpenProb: Float?,
        val rightEyeOpenProb: Float?,
        val noseX: Float?,
        val noseY: Float?,
        val leftEyeX: Float?,
        val leftEyeY: Float?,
        val rightEyeX: Float?,
        val rightEyeY: Float?,
        val boxWidth: Float,
        val boxHeight: Float
    )

    override fun reset() {
        synchronized(frameHistory) {
            frameHistory.clear()
        }
    }

    override fun processFrame(face: Face?, quality: QualityResult): LivenessResult {
        if (face == null || !quality.isAcceptable) {
            return LivenessResult(
                score = 0.0f,
                confidence = 0.0f,
                signals = LivenessSignals(0f, 0f, 0f, 0f, 0f),
                isLive = false,
                statusMessage = "Position face stably in frame"
            )
        }

        val noseLandmark = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
        val leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position

        val sample = FrameSample(
            timestamp = System.currentTimeMillis(),
            yaw = face.headEulerAngleY,
            pitch = face.headEulerAngleX,
            roll = face.headEulerAngleZ,
            leftEyeOpenProb = face.leftEyeOpenProbability,
            rightEyeOpenProb = face.rightEyeOpenProbability,
            noseX = noseLandmark?.x,
            noseY = noseLandmark?.y,
            leftEyeX = leftEyeLandmark?.x,
            leftEyeY = leftEyeLandmark?.y,
            rightEyeX = rightEyeLandmark?.x,
            rightEyeY = rightEyeLandmark?.y,
            boxWidth = face.boundingBox.width().toFloat(),
            boxHeight = face.boundingBox.height().toFloat()
        )

        val historyCopy: List<FrameSample>
        synchronized(frameHistory) {
            frameHistory.add(sample)
            if (frameHistory.size > WINDOW_SIZE) {
                frameHistory.removeFirst()
            }
            historyCopy = ArrayList(frameHistory)
        }

        if (historyCopy.size < MIN_FRAMES_FOR_DECISION) {
            return LivenessResult(
                score = 0.5f,
                confidence = (historyCopy.size.toFloat() / MIN_FRAMES_FOR_DECISION) * 0.5f,
                signals = LivenessSignals(0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
                isLive = false,
                statusMessage = "Analyzing temporal liveness..."
            )
        }

        // 1. Natural Landmark Micro-Movement (detects alive micromotions vs static photo or screen clamp)
        val landmarkMotionScore = computeLandmarkMotionScore(historyCopy)

        // 2. Eye Blink / Eye State Dynamics
        val eyeBlinkScore = computeEyeDynamicScore(historyCopy)

        // 3. 3D Pose Temporal Consistency (checks natural head respiration and 3D angle dynamics)
        val poseStabilityScore = computePoseDynamicsScore(historyCopy)

        // 4. Temporal Consistency (smooth continuous movement, no abrupt teleportation or frame cuts)
        val temporalConsistencyScore = computeTemporalConsistency(historyCopy)

        // 5. Texture & Blur Variance
        val textureVarianceScore = quality.blurScore.coerceIn(0f, 1f)

        // Composite Passive Liveness Score
        val compositeScore = (
            landmarkMotionScore * 0.30f +
            eyeBlinkScore * 0.25f +
            poseStabilityScore * 0.20f +
            temporalConsistencyScore * 0.15f +
            textureVarianceScore * 0.10f
        ).coerceIn(0f, 1f)

        val confidence = (historyCopy.size.toFloat() / WINDOW_SIZE).coerceIn(0f, 1f)
        val isLive = compositeScore >= 0.60f && landmarkMotionScore > 0.35f && temporalConsistencyScore > 0.40f

        val message = when {
            landmarkMotionScore < 0.20f -> "Static pattern detected. Ensure live presence."
            compositeScore < 0.50f -> "Evaluating face dynamics..."
            isLive -> "Passive liveness verified"
            else -> "Hold natural gaze"
        }

        return LivenessResult(
            score = compositeScore,
            confidence = confidence,
            signals = LivenessSignals(
                temporalConsistency = temporalConsistencyScore,
                landmarkMovement = landmarkMotionScore,
                eyeBlinkScore = eyeBlinkScore,
                poseStability = poseStabilityScore,
                textureVariance = textureVarianceScore
            ),
            isLive = isLive,
            statusMessage = message
        )
    }

    private fun computeLandmarkMotionScore(samples: List<FrameSample>): Float {
        if (samples.size < 3) return 0.5f
        var totalDistNorm = 0f
        var validPairs = 0

        for (i in 1 until samples.size) {
            val curr = samples[i]
            val prev = samples[i - 1]
            if (curr.noseX != null && curr.noseY != null && prev.noseX != null && prev.noseY != null) {
                val dx = curr.noseX - prev.noseX
                val dy = curr.noseY - prev.noseY
                val dist = sqrt(dx * dx + dy * dy)
                val normalizedDist = dist / (curr.boxWidth.coerceAtLeast(10f))
                totalDistNorm += normalizedDist
                validPairs++
            }
        }

        if (validPairs == 0) return 0.5f
        val avgNormalizedMotion = totalDistNorm / validPairs

        // Alive humans show natural micro-motions (0.001 to 0.04 normalized dist)
        return when {
            avgNormalizedMotion < STATIC_PHOTO_JITTER_THRESHOLD -> 0.15f // Frozen / printed paper
            avgNormalizedMotion > EXTREME_JITTER_THRESHOLD -> 0.30f // Severe shake
            else -> {
                val optimal = 0.005f
                val diff = abs(avgNormalizedMotion - optimal)
                (1f - (diff / 0.03f)).coerceIn(0.4f, 1.0f)
            }
        }
    }

    private fun computeEyeDynamicScore(samples: List<FrameSample>): Float {
        val leftEyeProbs = samples.mapNotNull { it.leftEyeOpenProb }
        val rightEyeProbs = samples.mapNotNull { it.rightEyeOpenProb }

        if (leftEyeProbs.size < 3 || rightEyeProbs.size < 3) return 0.60f

        val leftMax = leftEyeProbs.maxOrNull() ?: 1f
        val leftMin = leftEyeProbs.minOrNull() ?: 0f
        val leftRange = leftMax - leftMin

        val rightMax = rightEyeProbs.maxOrNull() ?: 1f
        val rightMin = rightEyeProbs.minOrNull() ?: 0f
        val rightRange = rightMax - rightMin

        val avgEyeRange = (leftRange + rightRange) / 2.0f
        // Dynamic variance or natural open eyes gives good score
        val bothEyesOpen = (leftEyeProbs.last() > 0.5f && rightEyeProbs.last() > 0.5f)

        return if (avgEyeRange > 0.15f) {
            // Blink detected during window
            0.95f
        } else if (bothEyesOpen) {
            0.75f
        } else {
            0.45f
        }
    }

    private fun computePoseDynamicsScore(samples: List<FrameSample>): Float {
        if (samples.size < 3) return 0.5f
        var yawVariance = 0f
        var pitchVariance = 0f

        val meanYaw = samples.map { it.yaw }.average().toFloat()
        val meanPitch = samples.map { it.pitch }.average().toFloat()

        samples.forEach {
            yawVariance += (it.yaw - meanYaw) * (it.yaw - meanYaw)
            pitchVariance += (it.pitch - meanPitch) * (it.pitch - meanPitch)
        }
        val stdYaw = sqrt(yawVariance / samples.size)
        val stdPitch = sqrt(pitchVariance / samples.size)

        // Natural living pose standard deviation is between 0.3 deg and 4.0 deg
        val combinedStd = (stdYaw + stdPitch) / 2.0f
        return when {
            combinedStd < 0.1f -> 0.35f // Completely static photo on stand
            combinedStd in 0.3f..5.0f -> 0.90f // Natural breathing / micro-yaw
            else -> 0.60f
        }
    }

    private fun computeTemporalConsistency(samples: List<FrameSample>): Float {
        if (samples.size < 3) return 0.8f
        var maxDiscontinuity = 0f

        for (i in 1 until samples.size) {
            val dt = (samples[i].timestamp - samples[i - 1].timestamp).coerceAtLeast(1L)
            val dyaw = abs(samples[i].yaw - samples[i - 1].yaw)
            val dpitch = abs(samples[i].pitch - samples[i - 1].pitch)
            val rate = (dyaw + dpitch) / (dt / 1000.0f) // Degrees per second
            if (rate > maxDiscontinuity) {
                maxDiscontinuity = rate
            }
        }

        // Extreme instantaneous jumps (> 90 deg/sec) indicate synthetic replay or cut
        return if (maxDiscontinuity > 90f) {
            0.20f
        } else {
            (1f - (maxDiscontinuity / 90f)).coerceIn(0.5f, 1.0f)
        }
    }
}
