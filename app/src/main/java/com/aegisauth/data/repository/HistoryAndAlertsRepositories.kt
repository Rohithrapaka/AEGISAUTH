package com.aegisauth.data.repository

import com.aegisauth.data.local.dao.AuthenticationEventDao
import com.aegisauth.data.local.dao.SecurityAlertDao
import com.aegisauth.data.local.entity.AuthenticationEvent
import com.aegisauth.data.local.entity.SecurityAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationHistoryRepository @Inject constructor(
    private val eventDao: AuthenticationEventDao
) {
    val allEventsFlow: Flow<List<AuthenticationEvent>> = eventDao.getAllEvents()
    val recentEventsFlow: Flow<List<AuthenticationEvent>> = eventDao.getRecentEvents(30)

    suspend fun recordEvent(event: AuthenticationEvent): Long = withContext(Dispatchers.IO) {
        eventDao.insert(event)
    }

    suspend fun getRecentFailedAttemptsCount(windowMinutes: Int = 10): Int = withContext(Dispatchers.IO) {
        val since = System.currentTimeMillis() - (windowMinutes * 60 * 1000L)
        eventDao.getRecentFailedAttemptsCount(since)
    }

    suspend fun getRecentFailedAttemptsCountForPackage(packageName: String, windowMinutes: Int = 10): Int = withContext(Dispatchers.IO) {
        if (packageName.isBlank()) {
            getRecentFailedAttemptsCount(windowMinutes)
        } else {
            val since = System.currentTimeMillis() - (windowMinutes * 60 * 1000L)
            eventDao.getRecentFailedAttemptsCountForPackage(packageName, since)
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        eventDao.clearAll()
    }
}

@Singleton
class SecurityAlertRepository @Inject constructor(
    private val alertDao: SecurityAlertDao
) {
    val allAlertsFlow: Flow<List<SecurityAlert>> = alertDao.getAllAlerts()
    val unreadAlertsFlow: Flow<List<SecurityAlert>> = alertDao.getUnreadAlerts()
    val unreadCountFlow: Flow<Int> = alertDao.getUnreadCount()

    suspend fun createAlert(
        severity: String,
        type: String,
        title: String,
        description: String
    ): Long = withContext(Dispatchers.IO) {
        alertDao.insert(
            SecurityAlert(
                severity = severity,
                type = type,
                title = title,
                description = description,
                isRead = false
            )
        )
    }

    suspend fun markAsRead(id: Long) = withContext(Dispatchers.IO) {
        alertDao.markAsRead(id)
    }

    suspend fun markAllAsRead() = withContext(Dispatchers.IO) {
        alertDao.markAllAsRead()
    }

    suspend fun clearAlerts() = withContext(Dispatchers.IO) {
        alertDao.clearAll()
    }
}
