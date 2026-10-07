package com.aegisauth.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "protected_applications",
    indices = [Index(value = ["packageName"], unique = true)]
)
data class ProtectedApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val isProtected: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "authentication_events")
data class AuthenticationEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val appName: String,
    val authMethod: String = "FACE", // FACE, SYSTEM_BIOMETRIC_BACKUP, PATTERN, PASSWORD
    val result: String, // SUCCESS, FAILURE, CHALLENGE_FAILED, TIMEOUT, CANCELLED
    val riskLevel: String, // LOW, MEDIUM, HIGH
    val livenessScore: Float,
    val qualityScore: Float,
    val similarityScore: Float,
    val challengeRequired: Boolean,
    val challengeResult: String?, // PASSED, FAILED, TIMED_OUT, SKIPPED
    val failureReason: String?,
    val evidencePath: String? = null
)

@Entity(tableName = "security_alerts")
data class SecurityAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: String, // INFO, WARNING, CRITICAL
    val type: String, // SPOOF_SUSPECTED, FAILED_ATTEMPTS, SERVICE_DISABLED, TEMPLATE_CHANGED, INTEGRITY_WARNING
    val title: String,
    val description: String,
    val isRead: Boolean = false
)

@Entity(tableName = "biometric_state")
data class BiometricState(
    @PrimaryKey
    val id: Int = 1, // Single active enrollment record
    val enrollmentVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val samplesCount: Int = 5,
    val templateHash: String, // Nonce/hash of the template identifier, never raw biometric data
    val profileId: Long = 0  // M15.1: 0 = legacy BIO 1, links to biometric_profiles
)

@Entity(tableName = "biometric_profiles")
data class BiometricProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    val createdAt: Long = System.currentTimeMillis()
)
