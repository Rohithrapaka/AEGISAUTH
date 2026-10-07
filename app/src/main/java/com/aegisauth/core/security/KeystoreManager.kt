package com.aegisauth.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class KeystoreManager @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val BIOHASH_KEY_ALIAS = "aegis_biohash_master_key"
        private const val PROJECTION_KEY_ALIAS = "aegis_projection_seed_key"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    private val keysInitialized: Boolean by lazy {
        ensureKeyExists(BIOHASH_KEY_ALIAS)
        ensureKeyExists(PROJECTION_KEY_ALIAS)
        true
    }

    /** Called automatically the first time any keystore operation is performed. */
    protected open fun ensureInitialized() {
        @Suppress("UNUSED_EXPRESSION")
        keysInitialized
    }

    private fun ensureKeyExists(alias: String) {
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(alias: String): SecretKey {
        return keyStore.getKey(alias, null) as SecretKey
    }

    /**
     * Encrypts raw bytes (e.g. BioHash template vector) using AES-256-GCM.
     * Returns a combined byte array: [12 bytes IV] + [Ciphertext with GCM tag]
     */
    fun encrypt(data: ByteArray): ByteArray {
        ensureInitialized()
        val cipher = Cipher.getInstance(AES_MODE)
        val secretKey = getSecretKey(BIOHASH_KEY_ALIAS)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)

        val byteBuffer = ByteBuffer.allocate(iv.size + ciphertext.size)
        byteBuffer.put(iv)
        byteBuffer.put(ciphertext)
        return byteBuffer.array()
    }

    /**
     * Decrypts combined IV + Ciphertext bytes using AES-256-GCM.
     */
    fun decrypt(encryptedData: ByteArray): ByteArray {
        ensureInitialized()
        require(encryptedData.size > GCM_IV_LENGTH) { "Encrypted data is too short" }
        val byteBuffer = ByteBuffer.wrap(encryptedData)
        val iv = ByteArray(GCM_IV_LENGTH)
        byteBuffer.get(iv)
        val ciphertext = ByteArray(byteBuffer.remaining())
        byteBuffer.get(ciphertext)

        val cipher = Cipher.getInstance(AES_MODE)
        val secretKey = getSecretKey(BIOHASH_KEY_ALIAS)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    /**
     * Obtains a deterministic pseudo-random seed derived from device keystore
     * to initialize the BioHash random projection matrix for this device.
     * Marked open so test subclasses can return a fixed seed without touching Android Keystore.
     */
    open fun getDeviceProjectionSeed(): Long {
        val seedBytes = encrypt("AEGIS_PROJECTION_SALT_V1".toByteArray(Charsets.UTF_8))
        val buffer = ByteBuffer.wrap(seedBytes)
        return buffer.long
    }

    /**
     * Securely generates random bytes.
     */
    fun generateSecureRandomBytes(length: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }

    /**
     * Deletes the biometric keys from Keystore on user wipe.
     */
    fun wipeBiometricKeys() {
        ensureInitialized()
        if (keyStore.containsAlias(BIOHASH_KEY_ALIAS)) {
            keyStore.deleteEntry(BIOHASH_KEY_ALIAS)
        }
        if (keyStore.containsAlias(PROJECTION_KEY_ALIAS)) {
            keyStore.deleteEntry(PROJECTION_KEY_ALIAS)
        }
        ensureKeyExists(BIOHASH_KEY_ALIAS)
        ensureKeyExists(PROJECTION_KEY_ALIAS)
    }
}
