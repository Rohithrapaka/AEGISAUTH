package com.aegisauth.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

@Singleton
open class SoundFeedbackManager @Inject constructor() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sampleRate = 44100

    enum class SoundEvent {
        VERIFICATION_STARTED,
        CHALLENGE_PROMPT,
        AUTH_SUCCESS,
        AUTH_FAILED,
        EVIDENCE_CAPTURED,
        LOCKOUT
    }

    open fun playSound(event: SoundEvent) {
        scope.launch {
            try {
                when (event) {
                    SoundEvent.VERIFICATION_STARTED -> playToneSweep(startFreq = 520.0, endFreq = 680.0, durationMs = 60, amplitude = 0.25f)
                    SoundEvent.CHALLENGE_PROMPT -> {
                        playSineTone(freq = 660.0, durationMs = 50, amplitude = 0.3f)
                        Thread.sleep(30)
                        playSineTone(freq = 880.0, durationMs = 50, amplitude = 0.3f)
                    }
                    SoundEvent.AUTH_SUCCESS -> {
                        playSineTone(freq = 587.33, durationMs = 70, amplitude = 0.35f) // D5
                        Thread.sleep(40)
                        playSineTone(freq = 880.0, durationMs = 120, amplitude = 0.4f)   // A5
                    }
                    SoundEvent.AUTH_FAILED -> {
                        playSineTone(freq = 240.0, durationMs = 90, amplitude = 0.4f)
                        Thread.sleep(40)
                        playSineTone(freq = 180.0, durationMs = 110, amplitude = 0.4f)
                    }
                    SoundEvent.EVIDENCE_CAPTURED -> {
                        playToneSweep(startFreq = 900.0, endFreq = 1400.0, durationMs = 45, amplitude = 0.3f)
                    }
                    SoundEvent.LOCKOUT -> {
                        playToneSweep(startFreq = 380.0, endFreq = 140.0, durationMs = 220, amplitude = 0.45f)
                    }
                }
            } catch (e: Exception) {
                // Audio failure should never crash the app
            }
        }
    }

    private fun playSineTone(freq: Double, durationMs: Int, amplitude: Float) {
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        val fadeSamples = (numSamples * 0.1f).toInt().coerceAtLeast(1)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / freq)
            var sample = (sin(angle) * Short.MAX_VALUE * amplitude).toInt().toShort()

            // Smooth linear attack/decay to prevent audio pop/click
            if (i < fadeSamples) {
                sample = (sample * (i.toFloat() / fadeSamples)).toInt().toShort()
            } else if (i > numSamples - fadeSamples) {
                sample = (sample * ((numSamples - i).toFloat() / fadeSamples)).toInt().toShort()
            }
            pcm[i] = sample
        }

        playPcmData(pcm)
    }

    private fun playToneSweep(startFreq: Double, endFreq: Double, durationMs: Int, amplitude: Float) {
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        val fadeSamples = (numSamples * 0.1f).toInt().coerceAtLeast(1)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val angle = 2.0 * Math.PI * currentFreq * t
            var sample = (sin(angle) * Short.MAX_VALUE * amplitude).toInt().toShort()

            if (i < fadeSamples) {
                sample = (sample * (i.toFloat() / fadeSamples)).toInt().toShort()
            } else if (i > numSamples - fadeSamples) {
                sample = (sample * ((numSamples - i).toFloat() / fadeSamples)).toInt().toShort()
            }
            pcm[i] = sample
        }

        playPcmData(pcm)
    }

    private fun playPcmData(pcm: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcm, 0, pcm.size)
        audioTrack.play()

        // Release track after playback completes
        Thread.sleep((pcm.size * 1000L / sampleRate) + 20)
        audioTrack.stop()
        audioTrack.release()
    }
}
