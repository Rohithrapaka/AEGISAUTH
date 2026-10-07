package com.aegisauth.feature.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.data.local.dao.BiometricStateDao
import com.aegisauth.data.local.entity.BiometricProfile
import com.aegisauth.data.local.entity.BiometricState
import com.aegisauth.data.repository.BiometricProfileRepository
import com.aegisauth.data.repository.BiometricTemplateRepository
import com.aegisauth.data.repository.SecurityAlertRepository
import com.aegisauth.core.biometric.SystemBiometricManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricUpdatesViewModel @Inject constructor(
    private val repository: BiometricTemplateRepository,
    private val biometricStateDao: BiometricStateDao,
    private val alertRepository: SecurityAlertRepository,
    private val profileRepository: BiometricProfileRepository,
    val systemBiometricManager: SystemBiometricManager
) : ViewModel() {

    val biometricState: StateFlow<BiometricState?> = biometricStateDao.getBiometricState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val profiles: StateFlow<List<BiometricProfile>> =
        profileRepository.getAllProfiles().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            profileRepository.ensureDefaultProfile()
        }
    }

    fun hasTemplateForProfile(profileId: Long): Boolean =
        profileRepository.hasTemplateFile(profileId)

    fun addProfile(displayName: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = profileRepository.insertProfile(displayName)
            if (newId > 0) onCreated(newId)
        }
    }

    fun renameProfile(profileId: Long, newName: String) {
        viewModelScope.launch {
            profileRepository.renameProfile(profileId, newName)
        }
    }

    fun deleteFaceTemplate(profileId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val templateFile = profileRepository.getTemplateFile(profileId)
            if (templateFile.exists()) templateFile.delete()
            alertRepository.createAlert(
                severity = "CRITICAL",
                type = "TEMPLATE_CHANGED",
                title = "Biometric Template Erased",
                description = "Face template for profile $profileId was deleted from device secure storage."
            )
            onDeleted()
        }
    }

    fun deleteProfile(profileId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
            alertRepository.createAlert(
                severity = "CRITICAL",
                type = "TEMPLATE_CHANGED",
                title = "Biometric Profile Deleted",
                description = "Biometric profile $profileId and its face template were permanently removed."
            )
            onDeleted()
        }
    }

    fun deleteTemplate(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTemplate()
            alertRepository.createAlert(
                severity = "CRITICAL",
                type = "TEMPLATE_CHANGED",
                title = "Biometric Template Erased",
                description = "Enrolled face template and cryptographic keys were wiped from device secure storage."
            )
            onDeleted()
        }
    }
}
