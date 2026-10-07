package com.aegisauth.feature.protectedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.data.repository.AppItem
import com.aegisauth.data.repository.ProtectedAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProtectedAppsViewModel @Inject constructor(
    private val repository: ProtectedAppsRepository
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = repository.getInstalledApplications()
            _installedApps.value = apps
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppProtection(app: AppItem, isProtected: Boolean) {
        viewModelScope.launch {
            repository.setAppProtection(app.packageName, app.appName, isProtected)
            // Update local state list
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == app.packageName) it.copy(isProtected = isProtected) else it
            }
        }
    }
}
