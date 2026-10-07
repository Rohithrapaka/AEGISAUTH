package com.aegisauth.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternSecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "aegis_pattern_vault"
        private const val KEY_PATTERN_HASH = "pattern_verifier_hash"
        private const val KEY_PATTERN_SALT = "pattern_verifier_salt"
        private const val MIN_PATTERN_LENGTH = 4
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasPattern(): Boolean {
        return securePrefs.contains(KEY_PATTERN_HASH) && securePrefs.contains(KEY_PATTERN_SALT)
    }

    fun savePattern(points: List<Int>): Boolean {
        if (points.size < MIN_PATTERN_LENGTH) return false

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val saltHex = salt.joinToString("") { "%02x".format(it) }

        val hashHex = computeSaltedHash(points, salt)

        securePrefs.edit()
            .putString(KEY_PATTERN_SALT, saltHex)
            .putString(KEY_PATTERN_HASH, hashHex)
            .apply()

        return true
    }

    fun verifyPattern(points: List<Int>): Boolean {
        if (!hasPattern() || points.size < MIN_PATTERN_LENGTH) return false

        val saltHex = securePrefs.getString(KEY_PATTERN_SALT, null) ?: return false
        val storedHashHex = securePrefs.getString(KEY_PATTERN_HASH, null) ?: return false

        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val computedHashHex = computeSaltedHash(points, salt)

        // Constant-time comparison to prevent timing side-channel attacks
        return MessageDigest.isEqual(storedHashHex.toByteArray(), computedHashHex.toByteArray())
    }

    fun clearPattern() {
        securePrefs.edit().clear().apply()
    }

    private fun computeSaltedHash(points: List<Int>, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val patternBytes = points.joinToString(",").toByteArray(Charsets.UTF_8)
        val hash = md.digest(patternBytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
