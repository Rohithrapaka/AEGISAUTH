package com.aegisauth.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages in-memory authenticated session state for AEGISAUTH self-protection.
 *
 * Security requirements (M15.2, M15.6):
 * - In-memory ONLY: Never persists "authenticated=true" flag to disk.
 * - Process death / reboot / app kill requires immediate re-authentication.
 * - Enforces configurable inactivity grace period (default 60s).
 * - Activity recreation (configuration changes) does not drop valid in-memory session.
 */
@Singleton
class AegisSessionManager @Inject constructor() {

    companion object {
        const val DEFAULT_GRACE_PERIOD_MS = 60_000L // 60 seconds inactivity threshold
    }

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    @Volatile
    private var lastAuthenticatedTimestamp: Long = 0L

    fun isSessionValid(gracePeriodMillis: Long = DEFAULT_GRACE_PERIOD_MS): Boolean {
        if (!_isAuthenticated.value) return false
        val elapsed = System.currentTimeMillis() - lastAuthenticatedTimestamp
        val valid = elapsed >= 0 && elapsed < gracePeriodMillis
        if (!valid) {
            // Auto-invalidate expired session in memory
            _isAuthenticated.value = false
            lastAuthenticatedTimestamp = 0L
        }
        return valid
    }

    fun authenticateSession() {
        lastAuthenticatedTimestamp = System.currentTimeMillis()
        _isAuthenticated.value = true
    }

    fun touchActivity() {
        if (_isAuthenticated.value) {
            val now = System.currentTimeMillis()
            if ((now - lastAuthenticatedTimestamp) < DEFAULT_GRACE_PERIOD_MS) {
                lastAuthenticatedTimestamp = now
            } else {
                invalidateSession()
            }
        }
    }

    fun invalidateSession() {
        _isAuthenticated.value = false
        lastAuthenticatedTimestamp = 0L
    }

    fun getLastAuthenticatedTimestamp(): Long = lastAuthenticatedTimestamp
}
