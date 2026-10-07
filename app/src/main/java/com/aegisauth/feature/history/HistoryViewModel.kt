package com.aegisauth.feature.history

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisauth.core.security.EvidenceManager
import com.aegisauth.data.local.entity.AuthenticationEvent
import com.aegisauth.data.repository.AuthenticationHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AuthenticationHistoryRepository,
    private val evidenceManager: EvidenceManager
) : ViewModel() {

    init {
        // Auto-purge unauthorized attempt captures older than 7-day retention window
        viewModelScope.launch(Dispatchers.IO) {
            evidenceManager.purgeExpiredEvidence(retentionDays = 7)
        }
    }

    val events: StateFlow<List<AuthenticationEvent>> = repository.allEventsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    suspend fun loadEvidenceBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
        evidenceManager.loadEvidenceBitmap(path)
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            evidenceManager.clearAllEvidence()
        }
    }
}
