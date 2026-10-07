package com.aegisauth.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages secure storage and verification of backup recovery passwords using PBKDF2-HMAC-SHA256.
 *
 * Security properties:
 * - PBKDF2-HMAC-SHA256 with 20,000 iterations and 256-bit derived key length.
 * - Unique cryptographically secure 16-byte random salt per credential.
 * - Constant-time verifier comparison using MessageDigest.isEqual.
 * - Stored in EncryptedSharedPreferences (AES256-GCM / AES256-SIV).
 * - Zero plaintext password storage, zero plaintext logging.
 */
@Singleton
class PasswordSecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "aegis_password_vault"
        private const val KEY_PASSWORD_HASH = "pwd_verifier_hash"
        private const val KEY_PASSWORD_SALT = "pwd_verifier_salt"
        private const val KEY_ITERATIONS = "pwd_verifier_iter"
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val DEFAULT_ITERATIONS = 20_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_LENGTH_BYTES = 16
        const val MIN_PASSWORD_LENGTH = 4

        /**
         * Pure static/isolated derivation helper for unit testing and domain verification.
         */
        fun deriveKeyHex(
            password: CharArray,
            salt: ByteArray,
            iterations: Int = DEFAULT_ITERATIONS,
            keyLengthBits: Int = KEY_LENGTH_BITS
        ): String {
            val spec = PBEKeySpec(password, salt, iterations, keyLengthBits)
            val skf = SecretKeyFactory.getInstance(ALGORITHM)
            val hash = skf.generateSecret(spec).encoded
            spec.clearPassword() // Zero out sensitive memory
            return hash.joinToString("") { "%02x".format(it) }
        }
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun hasPassword(): Boolean {
        return securePrefs.contains(KEY_PASSWORD_HASH) && securePrefs.contains(KEY_PASSWORD_SALT)
    }

    fun savePassword(password: CharArray): Boolean {
        if (password.size < MIN_PASSWORD_LENGTH) return false

        try {
            val salt = ByteArray(SALT_LENGTH_BYTES)
            SecureRandom().nextBytes(salt)
            val saltHex = salt.joinToString("") { "%02x".format(it) }

            val hashHex = deriveKeyHex(password, salt, DEFAULT_ITERATIONS)

            securePrefs.edit()
                .putString(KEY_PASSWORD_SALT, saltHex)
                .putString(KEY_PASSWORD_HASH, hashHex)
                .putInt(KEY_ITERATIONS, DEFAULT_ITERATIONS)
                .apply()

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun savePassword(password: String): Boolean {
        return savePassword(password.toCharArray())
    }

    fun verifyPassword(password: CharArray): Boolean {
        if (!hasPassword() || password.size < MIN_PASSWORD_LENGTH) return false

        try {
            val saltHex = securePrefs.getString(KEY_PASSWORD_SALT, null) ?: return false
            val storedHashHex = securePrefs.getString(KEY_PASSWORD_HASH, null) ?: return false
            val iterations = securePrefs.getInt(KEY_ITERATIONS, DEFAULT_ITERATIONS)

            val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val computedHashHex = deriveKeyHex(password, salt, iterations)

            // Constant-time comparison to prevent timing side-channel attacks
            return MessageDigest.isEqual(
                storedHashHex.toByteArray(Charsets.UTF_8),
                computedHashHex.toByteArray(Charsets.UTF_8)
            )
        } catch (e: Exception) {
            return false
        }
    }

    fun verifyPassword(password: String): Boolean {
        return verifyPassword(password.toCharArray())
    }

    fun clearPassword() {
        securePrefs.edit().clear().apply()
    }
}
