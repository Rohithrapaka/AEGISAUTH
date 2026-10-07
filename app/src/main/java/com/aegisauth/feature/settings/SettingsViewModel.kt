package com.aegisauth.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.data.datastore.AegisPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val livenessSensitivity: Float = 0.65f,
    val similarityThreshold: Float = 0.70f,
    val activeChallengeMode: String = "ADAPTIVE",
    val maxFailedAttempts: Int = 3,
    val unlockGracePeriodSecs: Int = 30,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AegisPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            preferences.livenessSensitivity,
            preferences.similarityThreshold,
            preferences.activeChallengeMode,
            preferences.maxFailedAttempts,
            preferences.unlockGracePeriodSecs
        ) { liveness, similarity, mode, maxFailed, gracePeriod ->
            listOf<Any>(liveness, similarity, mode, maxFailed, gracePeriod)
        },
        preferences.isHapticFeedbackEnabled,
        preferences.isNotificationsEnabled
    ) { partial, haptic, notifs ->
        @Suppress("UNCHECKED_CAST")
        val p = partial as List<Any>
        SettingsUiState(
            livenessSensitivity = p[0] as Float,
            similarityThreshold = p[1] as Float,
            activeChallengeMode = p[2] as String,
            maxFailedAttempts = p[3] as Int,
            unlockGracePeriodSecs = p[4] as Int,
            hapticFeedbackEnabled = haptic,
            notificationsEnabled = notifs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setLivenessSensitivity(value: Float) {
        viewModelScope.launch { preferences.setLivenessSensitivity(value) }
    }

    fun setSimilarityThreshold(value: Float) {
        viewModelScope.launch { preferences.setSimilarityThreshold(value) }
    }

    fun setActiveChallengeMode(mode: String) {
        viewModelScope.launch { preferences.setActiveChallengeMode(mode) }
    }

    fun setMaxFailedAttempts(attempts: Int) {
        viewModelScope.launch { preferences.setMaxFailedAttempts(attempts) }
    }

    fun setUnlockGracePeriod(seconds: Int) {
        viewModelScope.launch { preferences.setUnlockGracePeriodSecs(seconds) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { preferences.setHapticFeedbackEnabled(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }
}
