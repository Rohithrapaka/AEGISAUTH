package com.aegisauth.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aegis_settings")

@Singleton
class AegisPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PROTECTION_SERVICE_ENABLED = booleanPreferencesKey("protection_service_enabled")
        val LIVENESS_SENSITIVITY = floatPreferencesKey("liveness_sensitivity") // default 0.65f
        val SIMILARITY_THRESHOLD = floatPreferencesKey("similarity_threshold") // default 0.70f
        val ACTIVE_CHALLENGE_MODE = stringPreferencesKey("active_challenge_mode") // ALWAYS, ADAPTIVE, NEVER
        val MAX_FAILED_ATTEMPTS = intPreferencesKey("max_failed_attempts") // default 3
        val LOCKOUT_DURATION_MINS = intPreferencesKey("lockout_duration_mins") // default 5
        val UNLOCK_GRACE_PERIOD_SECS = intPreferencesKey("unlock_grace_period_secs") // default 30
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled") // default true
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled") // default true
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val isProtectionServiceEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.PROTECTION_SERVICE_ENABLED] ?: true
    }

    val livenessSensitivity: Flow<Float> = context.dataStore.data.map {
        it[Keys.LIVENESS_SENSITIVITY] ?: 0.65f
    }

    val similarityThreshold: Flow<Float> = context.dataStore.data.map {
        it[Keys.SIMILARITY_THRESHOLD] ?: 0.70f
    }

    val activeChallengeMode: Flow<String> = context.dataStore.data.map {
        it[Keys.ACTIVE_CHALLENGE_MODE] ?: "ADAPTIVE"
    }

    val maxFailedAttempts: Flow<Int> = context.dataStore.data.map {
        it[Keys.MAX_FAILED_ATTEMPTS] ?: 3
    }

    val lockoutDurationMins: Flow<Int> = context.dataStore.data.map {
        it[Keys.LOCKOUT_DURATION_MINS] ?: 5
    }

    val unlockGracePeriodSecs: Flow<Int> = context.dataStore.data.map {
        it[Keys.UNLOCK_GRACE_PERIOD_SECS] ?: 30
    }

    val isHapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.HAPTIC_FEEDBACK_ENABLED] ?: true
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setProtectionServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PROTECTION_SERVICE_ENABLED] = enabled }
    }

    suspend fun setLivenessSensitivity(value: Float) {
        context.dataStore.edit { it[Keys.LIVENESS_SENSITIVITY] = value }
    }

    suspend fun setSimilarityThreshold(value: Float) {
        context.dataStore.edit { it[Keys.SIMILARITY_THRESHOLD] = value }
    }

    suspend fun setActiveChallengeMode(mode: String) {
        context.dataStore.edit { it[Keys.ACTIVE_CHALLENGE_MODE] = mode }
    }

    suspend fun setMaxFailedAttempts(attempts: Int) {
        context.dataStore.edit { it[Keys.MAX_FAILED_ATTEMPTS] = attempts }
    }

    suspend fun setLockoutDurationMins(duration: Int) {
        context.dataStore.edit { it[Keys.LOCKOUT_DURATION_MINS] = duration }
    }

    suspend fun setUnlockGracePeriodSecs(seconds: Int) {
        context.dataStore.edit { it[Keys.UNLOCK_GRACE_PERIOD_SECS] = seconds }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_FEEDBACK_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
