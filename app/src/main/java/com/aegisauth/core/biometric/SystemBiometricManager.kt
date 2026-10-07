package com.aegisauth.core.biometric

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulates Android platform BiometricPrompt as an authorized SYSTEM_BIOMETRIC_BACKUP mechanism.
 *
 * Strict security invariants (M15.2, M15.3):
 * - Zero custom fingerprint capture, image processing, or raw sensor handling.
 * - Receives ONLY high-level system authentication callback results from Android OS.
 * - Zero fingerprint credential storage in AEGISAUTH databases.
 * - Never claims a biometric is available unless Android framework reports it.
 */
@Singleton
class SystemBiometricManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AegisBiometric"
        const val AUTH_METHOD_SYSTEM_BIOMETRIC = "SYSTEM_BIOMETRIC_BACKUP"
    }

    enum class BiometricHardwareStatus {
        AVAILABLE,
        NO_HARDWARE,
        HARDWARE_UNAVAILABLE,
        NONE_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNSUPPORTED
    }

    sealed class BiometricPromptResult {
        object Success : BiometricPromptResult()
        object Failed : BiometricPromptResult()
        object UserCanceled : BiometricPromptResult()
        object TemporaryLockout : BiometricPromptResult()
        object PermanentLockout : BiometricPromptResult()
        data class Error(val errorCode: Int, val errString: CharSequence) : BiometricPromptResult()
    }

    fun checkBiometricStatus(customContext: Context = context): BiometricHardwareStatus {
        val biometricManager = BiometricManager.from(customContext)
        val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricHardwareStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricHardwareStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricHardwareStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricHardwareStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricHardwareStatus.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricHardwareStatus.UNSUPPORTED
            else -> BiometricHardwareStatus.UNSUPPORTED
        }
    }

    fun isSystemBiometricAvailable(customContext: Context = context): Boolean {
        return checkBiometricStatus(customContext) == BiometricHardwareStatus.AVAILABLE
    }

    fun getUiActionLabel(customContext: Context = context): String {
        val pm = customContext.packageManager
        val hasFingerprint = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        return if (hasFingerprint) "USE FINGERPRINT" else "USE DEVICE BIOMETRIC"
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Biometric Verification",
        subtitle: String = "Confirm your identity with system biometric",
        negativeButtonText: String = "Use Recovery PIN / Pattern",
        onResult: (BiometricPromptResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .setConfirmationRequired(false)
            .build()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "System biometric authentication succeeded (type: SYSTEM_BIOMETRIC_BACKUP)")
                onResult(BiometricPromptResult.Success)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "System biometric authentication failed (unrecognized biometric)")
                onResult(BiometricPromptResult.Failed)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "System biometric error: code=$errorCode, message=$errString")
                val result = when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> BiometricPromptResult.UserCanceled

                    BiometricPrompt.ERROR_LOCKOUT -> BiometricPromptResult.TemporaryLockout
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricPromptResult.PermanentLockout
                    else -> BiometricPromptResult.Error(errorCode, errString)
                }
                onResult(result)
            }
        }

        try {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch BiometricPrompt: ${e.localizedMessage}")
            onResult(BiometricPromptResult.Error(-1, e.localizedMessage ?: "Prompt failed"))
        }
    }
}
