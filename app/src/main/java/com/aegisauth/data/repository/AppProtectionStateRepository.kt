package com.aegisauth.data.repository

import android.content.Context
import android.util.Log
import com.aegisauth.service.AppProtectionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

enum class ServiceRuntimeState {
    DISABLED,
    PERMISSION_GRANTED_SERVICE_DISCONNECTED,
    SERVICE_CONNECTING,
    SERVICE_CONNECTED,
    INTERCEPTION_INITIALIZING,
    INTERCEPTION_ACTIVE,
    SERVICE_ERROR
}

@Singleton
class AppProtectionStateRepository @Inject constructor() {

    companion object {
        private const val TAG = "AegisProtectionState"
        const val HEARTBEAT_STALE_THRESHOLD_MS = 6000L
        private val INSTANCE_STATE = MutableStateFlow(ServiceRuntimeState.DISABLED)
        private val LAST_HEARTBEAT = MutableStateFlow(0L)
        private val IS_INITIALIZED = MutableStateFlow(false)
        private val PROTECTED_COUNT = MutableStateFlow(0)
    }

    val runtimeState: Flow<ServiceRuntimeState> = INSTANCE_STATE.asStateFlow()
    val lastHeartbeat: Flow<Long> = LAST_HEARTBEAT.asStateFlow()

    fun recordHeartbeat() {
        LAST_HEARTBEAT.value = System.currentTimeMillis()
    }

    fun updateServiceConnected(connected: Boolean) {
        Log.d(TAG, if (connected) "SERVICE_CONNECTED" else "SERVICE_DISCONNECTED")
        if (connected) {
            recordHeartbeat()
            INSTANCE_STATE.value = ServiceRuntimeState.SERVICE_CONNECTED
        } else {
            INSTANCE_STATE.value = ServiceRuntimeState.PERMISSION_GRANTED_SERVICE_DISCONNECTED
            IS_INITIALIZED.value = false
        }
    }

    fun updateInitializationState(initialized: Boolean, protectedCount: Int) {
        IS_INITIALIZED.value = initialized
        PROTECTED_COUNT.value = protectedCount
        if (initialized) {
            Log.d(TAG, "INTERCEPTION_READY: $protectedCount packages protected")
            INSTANCE_STATE.value = ServiceRuntimeState.INTERCEPTION_ACTIVE
        } else {
            INSTANCE_STATE.value = ServiceRuntimeState.INTERCEPTION_INITIALIZING
        }
    }

    fun updateErrorState(errorMsg: String) {
        Log.e(TAG, "SERVICE_ERROR: $errorMsg")
        INSTANCE_STATE.value = ServiceRuntimeState.SERVICE_ERROR
    }

    /**
     * Authoritative real-time flow combining:
     * 1. Android system Accessibility permission state
     * 2. Live service runtime heartbeat
     * 3. Initialization & cache readiness
     */
    fun getAuthoritativeStateFlow(context: Context): Flow<ServiceRuntimeState> {
        val ticker = flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(2000L)
            }
        }

        return combine(
            INSTANCE_STATE,
            LAST_HEARTBEAT,
            ticker
        ) { state, lastHb, now ->
            val isPermissionEnabled = AppProtectionService.isAccessibilityServiceEnabled(context)
            if (!isPermissionEnabled) {
                ServiceRuntimeState.DISABLED
            } else {
                val isHeartbeatFresh = (now - lastHb) < HEARTBEAT_STALE_THRESHOLD_MS
                if (!isHeartbeatFresh) {
                    Log.d(TAG, "HEARTBEAT_STALE: lastHb=$lastHb, now=$now, diff=${now - lastHb}ms (stale threshold=${HEARTBEAT_STALE_THRESHOLD_MS}ms)")
                    ServiceRuntimeState.PERMISSION_GRANTED_SERVICE_DISCONNECTED
                } else {
                    state
                }
            }
        }
    }
}
