package com.aegisauth.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.data.datastore.AegisPreferences
import com.aegisauth.data.repository.BiometricTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aegisauth.core.security.AegisSessionManager

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Onboarding : SplashDestination()
    object Enrollment : SplashDestination()
    object Dashboard : SplashDestination()
    object SelfAuthentication : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferences: AegisPreferences,
    private val biometricRepository: BiometricTemplateRepository,
    private val sessionManager: AegisSessionManager
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination = _destination.asStateFlow()

    init {
        checkInitialDestination()
    }

    private fun checkInitialDestination() {
        viewModelScope.launch {
            delay(1200) // Smooth brand splash
            val onboardingCompleted = preferences.isOnboardingCompleted.first()
            val hasTemplate = biometricRepository.hasEnrolledTemplate()

            _destination.value = when {
                !onboardingCompleted -> SplashDestination.Onboarding
                !hasTemplate -> SplashDestination.Enrollment
                sessionManager.isSessionValid() -> SplashDestination.Dashboard
                else -> SplashDestination.SelfAuthentication
            }
        }
    }
}
