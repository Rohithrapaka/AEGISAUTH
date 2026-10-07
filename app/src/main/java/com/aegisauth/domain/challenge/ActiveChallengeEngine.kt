package com.aegisauth.domain.challenge

import com.aegisauth.domain.model.ChallengeState
import com.aegisauth.domain.model.ChallengeType
import com.google.mlkit.vision.face.Face
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

interface ChallengeEngine {
    fun startNewChallenge(allowedTypes: List<ChallengeType> = ChallengeType.values().toList()): ChallengeState
    fun evaluateFace(face: Face?, currentState: ChallengeState): ChallengeState
    fun tickTimer(currentState: ChallengeState): ChallengeState
    fun reset()
}

@Singleton
class ActiveChallengeEngine @Inject constructor() : ChallengeEngine {

    private val secureRandom = SecureRandom()
    private var initialEulerYaw = 0f
    private var initialEulerPitch = 0f
    private var eyeClosedSeen = false

    override fun reset() {
        initialEulerYaw = 0f
        initialEulerPitch = 0f
        eyeClosedSeen = false
    }

    override fun startNewChallenge(allowedTypes: List<ChallengeType>): ChallengeState {
        reset()
        val types = if (allowedTypes.isNotEmpty()) allowedTypes else ChallengeType.values().toList()
        val randomIndex = secureRandom.nextInt(types.size)
        val selected = types[randomIndex]
        return ChallengeState(
            challenge = selected,
            progress = 0f,
            remainingSeconds = 8,
            isCompleted = false,
            isFailed = false
        )
    }

    override fun tickTimer(currentState: ChallengeState): ChallengeState {
        if (currentState.isCompleted || currentState.isFailed) return currentState
        val remaining = currentState.remainingSeconds - 1
        return if (remaining <= 0) {
            currentState.copy(remainingSeconds = 0, isFailed = true)
        } else {
            currentState.copy(remainingSeconds = remaining)
        }
    }

    override fun evaluateFace(face: Face?, currentState: ChallengeState): ChallengeState {
        if (currentState.isCompleted || currentState.isFailed) return currentState
        if (face == null) return currentState

        val yaw = face.headEulerAngleY
        val pitch = face.headEulerAngleX
        val leftEye = face.leftEyeOpenProbability ?: 0.5f
        val rightEye = face.rightEyeOpenProbability ?: 0.5f
        val smiling = face.smilingProbability ?: 0.0f

        var progress = 0.0f
        var isPassed = false

        when (currentState.challenge) {
            ChallengeType.BLINK -> {
                // Requires both eyes to close (< 0.25) then open (> 0.65)
                if (leftEye < 0.25f && rightEye < 0.25f) {
                    eyeClosedSeen = true
                    progress = 0.5f
                } else if (eyeClosedSeen && leftEye > 0.65f && rightEye > 0.65f) {
                    progress = 1.0f
                    isPassed = true
                } else if (eyeClosedSeen) {
                    progress = 0.7f
                }
            }

            ChallengeType.TURN_LEFT -> {
                // ML Kit: negative yaw = face turning left (from camera perspective)
                if (yaw < -12f) {
                    progress = ((-yaw) / 25f).coerceIn(0f, 1f)
                    if (progress >= 0.85f) isPassed = true
                }
            }

            ChallengeType.TURN_RIGHT -> {
                // ML Kit: positive yaw = face turning right (from camera perspective)
                if (yaw > 12f) {
                    progress = (yaw / 25f).coerceIn(0f, 1f)
                    if (progress >= 0.85f) isPassed = true
                }
            }

            ChallengeType.LOOK_UP -> {
                if (pitch > 10f) {
                    progress = (pitch / 16f).coerceIn(0f, 1f)
                    if (progress >= 0.95f) isPassed = true
                }
            }

            ChallengeType.LOOK_DOWN -> {
                if (pitch < -10f) {
                    progress = (abs(pitch) / 16f).coerceIn(0f, 1f)
                    if (progress >= 0.95f) isPassed = true
                }
            }

            ChallengeType.SMILE -> {
                progress = (smiling / 0.65f).coerceIn(0f, 1f)
                if (smiling >= 0.65f) {
                    isPassed = true
                }
            }
        }

        return currentState.copy(
            progress = progress.coerceAtLeast(currentState.progress),
            isCompleted = isPassed
        )
    }
}
