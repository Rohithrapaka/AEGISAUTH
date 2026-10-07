package com.aegisauth.data.local.dao

import androidx.room.*
import com.aegisauth.data.local.entity.AuthenticationEvent
import com.aegisauth.data.local.entity.BiometricProfile
import com.aegisauth.data.local.entity.BiometricState
import com.aegisauth.data.local.entity.ProtectedApplication
import com.aegisauth.data.local.entity.SecurityAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectedAppDao {
    @Query("SELECT * FROM protected_applications ORDER BY appName ASC")
    fun getAllProtectedApps(): Flow<List<ProtectedApplication>>

    @Query("SELECT * FROM protected_applications WHERE isProtected = 1")
    fun getActiveProtectedApps(): Flow<List<ProtectedApplication>>

    @Query("SELECT * FROM protected_applications WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): ProtectedApplication?

    @Query("SELECT EXISTS(SELECT 1 FROM protected_applications WHERE packageName = :packageName AND isProtected = 1)")
    suspend fun isAppProtected(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: ProtectedApplication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<ProtectedApplication>)

    @Query("UPDATE protected_applications SET isProtected = :isProtected, updatedAt = :updatedAt WHERE packageName = :packageName")
    suspend fun updateProtectionStatus(packageName: String, isProtected: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(app: ProtectedApplication)

    @Query("DELETE FROM protected_applications WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}

@Dao
interface AuthenticationEventDao {
    @Query("SELECT * FROM authentication_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<AuthenticationEvent>>

    @Query("SELECT * FROM authentication_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 20): Flow<List<AuthenticationEvent>>

    @Query("SELECT * FROM authentication_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getEventsSince(sinceTimestamp: Long): List<AuthenticationEvent>

    @Query("SELECT COUNT(*) FROM authentication_events WHERE result = 'FAILURE' AND timestamp >= :sinceTimestamp")
    suspend fun getRecentFailedAttemptsCount(sinceTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM authentication_events WHERE result = 'FAILURE' AND packageName = :packageName AND timestamp >= :sinceTimestamp")
    suspend fun getRecentFailedAttemptsCountForPackage(packageName: String, sinceTimestamp: Long): Int

    @Insert
    suspend fun insert(event: AuthenticationEvent): Long

    @Query("DELETE FROM authentication_events")
    suspend fun clearAll()
}

@Dao
interface SecurityAlertDao {
    @Query("SELECT * FROM security_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<SecurityAlert>>

    @Query("SELECT * FROM security_alerts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadAlerts(): Flow<List<SecurityAlert>>

    @Query("SELECT COUNT(*) FROM security_alerts WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(alert: SecurityAlert): Long

    @Query("UPDATE security_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE security_alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM security_alerts")
    suspend fun clearAll()
}

@Dao
interface BiometricStateDao {
    @Query("SELECT * FROM biometric_state WHERE id = 1 LIMIT 1")
    fun getBiometricState(): Flow<BiometricState?>

    @Query("SELECT * FROM biometric_state WHERE id = 1 LIMIT 1")
    suspend fun getBiometricStateSync(): BiometricState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: BiometricState)

    @Query("DELETE FROM biometric_state WHERE id = 1")
    suspend fun deleteState()
}

// M15.1 — Multi-profile biometric management
@Dao
interface BiometricProfileDao {
    @Query("SELECT * FROM biometric_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<BiometricProfile>>

    @Query("SELECT * FROM biometric_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: Long): BiometricProfile?

    @Query("SELECT COUNT(*) FROM biometric_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BiometricProfile): Long

    @Update
    suspend fun updateProfile(profile: BiometricProfile)

    @Query("DELETE FROM biometric_profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Long)
}
