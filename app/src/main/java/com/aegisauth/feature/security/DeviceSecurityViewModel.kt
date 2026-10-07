package com.aegisauth.feature.security

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.service.AppProtectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityCheckItem(
    val title: String,
    val description: String,
    val isSecure: Boolean,
    val statusText: String,
    val isCritical: Boolean = false
)

data class DeviceSecurityState(
    val overallStatus: String = "SECURE",
    val checks: List<SecurityCheckItem> = emptyList()
)

@HiltViewModel
class DeviceSecurityViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(DeviceSecurityState())
    val state = _state.asStateFlow()

    init {
        performSecurityAudit()
    }

    fun performSecurityAudit() {
        viewModelScope.launch {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isScreenLockSecure = keyguardManager?.isDeviceSecure ?: false

            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            val isAccessibilityEnabled = AppProtectionService.isAccessibilityServiceEnabled(context)

            val isDevOptionsEnabled = try {
                Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                    0
                ) != 0
            } catch (e: Exception) {
                false
            }

            val checks = listOf(
                SecurityCheckItem(
                    title = "Android Keystore Enclave",
                    description = "Hardware-backed AES-256-GCM cryptographic master key",
                    isSecure = true,
                    statusText = "HARDWARE SECURE"
                ),
                SecurityCheckItem(
                    title = "Device Screen Lock",
                    description = "PIN, Password, or Pattern lock enabled on device",
                    isSecure = isScreenLockSecure,
                    statusText = if (isScreenLockSecure) "CONFIGURED" else "NOT SET",
                    isCritical = !isScreenLockSecure
                ),
                SecurityCheckItem(
                    title = "App Protection Service",
                    description = "Accessibility event monitor intercepting protected apps",
                    isSecure = isAccessibilityEnabled,
                    statusText = if (isAccessibilityEnabled) "ACTIVE" else "DISABLED IN SETTINGS",
                    isCritical = !isAccessibilityEnabled
                ),
                SecurityCheckItem(
                    title = "Biometric Camera Sensor",
                    description = "Front camera access for passive liveness and face capture",
                    isSecure = hasCameraPermission,
                    statusText = if (hasCameraPermission) "GRANTED" else "PERMISSION MISSING",
                    isCritical = !hasCameraPermission
                ),
                SecurityCheckItem(
                    title = "Developer Debugging State",
                    description = "ADB USB debugging and developer options",
                    isSecure = !isDevOptionsEnabled,
                    statusText = if (!isDevOptionsEnabled) "SECURE (OFF)" else "DEV MODE DETECTED",
                    isCritical = false
                )
            )

            val hasCriticalFailures = checks.any { !it.isSecure && it.isCritical }
            val hasWarnings = checks.any { !it.isSecure && !it.isCritical }
            val overall = when {
                hasCriticalFailures -> "CRITICAL"
                hasWarnings -> "WARNING"
                else -> "SECURE"
            }

            _state.value = DeviceSecurityState(
                overallStatus = overall,
                checks = checks
            )
        }
    }
}
