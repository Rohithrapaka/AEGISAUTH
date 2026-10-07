package com.aegisauth.domain

import org.junit.Assert.*
import org.junit.Test

class M151AuthFlowTest {

    sealed class AuthFlowState {
        object FaceSearching : AuthFlowState()
        object FaceFailed : AuthFlowState()
        object SystemBiometricFailed : AuthFlowState()
        object RecoveryFallback : AuthFlowState()
        object AccessDenied : AuthFlowState()
        object Authenticated : AuthFlowState()
    }

    data class AuthSession(
        val packageName: String,
        val hasRecovery: Boolean
    ) {
        var state: AuthFlowState = AuthFlowState.FaceSearching
        var faceFailed = false
        var sysBioFailed = false

        fun onFaceFailure() {
            faceFailed = true
            state = AuthFlowState.FaceFailed
        }

        fun onSystemBiometricFailure() {
            sysBioFailed = true
            state = if (packageName == "com.aegisauth" && hasRecovery)
                AuthFlowState.RecoveryFallback
            else
                AuthFlowState.AccessDenied
        }

        fun canShowRecovery(): Boolean =
            state is AuthFlowState.RecoveryFallback
    }

    @Test
    fun `initial state is FaceSearching`() {
        val session = AuthSession("com.aegisauth", true)
        assertTrue(session.state is AuthFlowState.FaceSearching)
    }

    @Test
    fun `face failure does not show recovery`() {
        val session = AuthSession("com.aegisauth", true)
        session.onFaceFailure()
        assertFalse(session.canShowRecovery())
        assertTrue(session.state is AuthFlowState.FaceFailed)
    }

    @Test
    fun `after system biometric failure on aegisauth, recovery is shown`() {
        val session = AuthSession("com.aegisauth", true)
        session.onFaceFailure()
        session.onSystemBiometricFailure()
        assertTrue(session.canShowRecovery())
        assertTrue(session.state is AuthFlowState.RecoveryFallback)
    }

    @Test
    fun `after system biometric failure on third-party app, access is denied`() {
        val session = AuthSession("com.example.app", true)
        session.onFaceFailure()
        session.onSystemBiometricFailure()
        assertFalse(session.canShowRecovery())
        assertTrue(session.state is AuthFlowState.AccessDenied)
    }

    @Test
    fun `third-party app never shows recovery even if recovery is configured`() {
        val session = AuthSession("com.third.party", true)
        session.onSystemBiometricFailure()
        assertFalse(session.canShowRecovery())
    }

    @Test
    fun `face searching state never shows recovery`() {
        val session = AuthSession("com.aegisauth", true)
        assertFalse(session.canShowRecovery())
    }

    @Test
    fun `no recovery configured, recovery fallback not shown`() {
        val session = AuthSession("com.aegisauth", false)
        session.onFaceFailure()
        session.onSystemBiometricFailure()
        assertFalse(session.canShowRecovery())
    }

    @Test
    fun `recovery only after full biometric chain failure`() {
        val session = AuthSession("com.aegisauth", true)
        assertFalse(session.canShowRecovery())
        session.onFaceFailure()
        assertFalse(session.canShowRecovery())
        session.onSystemBiometricFailure()
        assertTrue(session.canShowRecovery())
    }

    @Test
    fun `authenticated state — not recovery`() {
        val session = AuthSession("com.aegisauth", true)
        session.state = AuthFlowState.Authenticated
        assertFalse(session.canShowRecovery())
    }

    @Test
    fun `access denied is terminal for third-party`() {
        val session = AuthSession("com.anotherapp", true)
        session.onFaceFailure()
        session.onSystemBiometricFailure()
        assertTrue(session.state is AuthFlowState.AccessDenied)
        assertFalse(session.canShowRecovery())
    }
}
