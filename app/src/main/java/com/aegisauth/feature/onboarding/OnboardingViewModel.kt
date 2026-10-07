package com.aegisauth.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.data.datastore.AegisPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val tag: String
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: AegisPreferences
) : ViewModel() {

    val pages = listOf(
        OnboardingPage(
            title = "Zero-Trust\nBiometrics",
            subtitle = "PASSIVE ANTI-SPOOFING",
            description = "AEGISAUTH never trusts a face image simply because a face is detected. Real-time multi-signal passive liveness checks eye dynamics, micro-motion, and 3D pose stability.",
            tag = "01 // VISION ENGINE"
        ),
        OnboardingPage(
            title = "Protected\nBioHash Templates",
            subtitle = "NON-REVERSIBLE CREDENTIALS",
            description = "Your raw facial imagery is never saved. Biometric vectors are transformed into randomized orthonormal BioHash representations protected by Android Keystore hardware keys.",
            tag = "02 // CRYPTO ENCLAVE"
        ),
        OnboardingPage(
            title = "Application\nGuard Service",
            subtitle = "ZERO-LATENCY INTERCEPTION",
            description = "Secure your most sensitive apps like WhatsApp, Gallery, Banking, and Settings with instant biometric validation whenever they are launched.",
            tag = "03 // APP SHIELD"
        )
    )

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    fun onNextPage(onComplete: () -> Unit) {
        if (_currentPage.value < pages.size - 1) {
            _currentPage.value += 1
        } else {
            completeOnboarding(onComplete)
        }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            preferences.setOnboardingCompleted(true)
            onComplete()
        }
    }
}
