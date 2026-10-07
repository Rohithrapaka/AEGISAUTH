package com.aegisauth.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.data.datastore.AegisPreferences
import com.aegisauth.data.local.entity.AuthenticationEvent
import com.aegisauth.data.local.entity.ProtectedApplication
import com.aegisauth.data.local.entity.SecurityAlert
import com.aegisauth.data.repository.AppProtectionStateRepository
import com.aegisauth.data.repository.AuthenticationHistoryRepository
import com.aegisauth.data.repository.BiometricTemplateRepository
import com.aegisauth.data.repository.ProtectedAppsRepository
import com.aegisauth.data.repository.SecurityAlertRepository
import com.aegisauth.data.repository.ServiceRuntimeState
import com.aegisauth.service.AppProtectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isProtectionEnabled: Boolean = true,
    val isBiometricEnrolled: Boolean = true,
    val protectedAppsCount: Int = 0,
    val recentEvents: List<AuthenticationEvent> = emptyList(),
    val unreadAlertsCount: Int = 0,
    val securityStatus: String = "SECURE",
    val serviceRuntimeState: ServiceRuntimeState = ServiceRuntimeState.DISABLED,
    val isServiceRunning: Boolean = false,
    val isAccessibilityEnabledInSettings: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: AegisPreferences,
    private val biometricRepository: BiometricTemplateRepository,
    private val protectedAppsRepository: ProtectedAppsRepository,
    private val historyRepository: AuthenticationHistoryRepository,
    private val alertRepository: SecurityAlertRepository,
    private val appProtectionStateRepository: AppProtectionStateRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    fun checkAccessibilityState() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(preferences.isProtectionServiceEnabled, biometricRepository.isEnrolled, ::Pair),
        protectedAppsRepository.protectedAppsFlow,
        historyRepository.recentEventsFlow,
        alertRepository.unreadCountFlow,
        appProtectionStateRepository.getAuthoritativeStateFlow(context)
    ) { (isProtEnabled, isEnrolled), apps, events, unreadAlerts, runtimeState ->
        val isAccEnabled = AppProtectionService.isAccessibilityServiceEnabled(context)
        val protectedCount = apps.count { it.isProtected }
        val isServiceActive = isProtEnabled && (runtimeState == ServiceRuntimeState.INTERCEPTION_ACTIVE || runtimeState == ServiceRuntimeState.SERVICE_CONNECTED)

        val securityStatus = when {
            !isAccEnabled -> "SERVICE_REQUIRED"
            runtimeState == ServiceRuntimeState.PERMISSION_GRANTED_SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
            unreadAlerts > 0 -> "WARNING"
            else -> "SECURE"
        }

        DashboardUiState(
            isProtectionEnabled = isProtEnabled,
            isBiometricEnrolled = isEnrolled,
            protectedAppsCount = protectedCount,
            recentEvents = events.take(5),
            unreadAlertsCount = unreadAlerts,
            securityStatus = securityStatus,
            serviceRuntimeState = runtimeState,
            isServiceRunning = isServiceActive,
            isAccessibilityEnabledInSettings = isAccEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun toggleProtectionService(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setProtectionServiceEnabled(enabled)
            if (!enabled) {
                alertRepository.createAlert(
                    severity = "WARNING",
                    type = "SERVICE_DISABLED",
                    title = "App Protection Disabled",
                    description = "AEGISAUTH protection service was manually turned off. Protected applications will not require biometric verification."
                )
            }
        }
    }
}

