package com.aegisauth.core.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EvidenceManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager
) {

    private val evidenceDir: File by lazy {
        val dir = File(context.filesDir, "evidence")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    /**
     * Compresses the probe face bitmap to JPEG, encrypts it via Keystore AES-256-GCM,
     * and saves it to app-private storage.
     *
     * @return File path of the encrypted evidence artifact, or null if capture failed.
     */
    open fun captureEvidence(
        faceBitmap: Bitmap,
        packageName: String
    ): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            faceBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val rawJpegBytes = outputStream.toByteArray()

            if (rawJpegBytes.isEmpty()) return null

            val encryptedBytes = keystoreManager.encrypt(rawJpegBytes)
            val fileName = "ev_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.enc"
            val targetFile = File(evidenceDir, fileName)

            targetFile.writeBytes(encryptedBytes)
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads and decrypts an evidence bitmap from disk.
     */
    open fun loadEvidenceBitmap(evidencePath: String): Bitmap? {
        return try {
            val file = File(evidencePath)
            if (!file.exists()) return null

            val encryptedBytes = file.readBytes()
            val decryptedBytes = keystoreManager.decrypt(encryptedBytes)
            BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Purges evidence files older than the retention period (default 7 days).
     *
     * @return Number of deleted files.
     */
    open fun purgeExpiredEvidence(retentionDays: Int = 7): Int {
        var deletedCount = 0
        try {
            val cutoff = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L)
            val files = evidenceDir.listFiles { file -> file.isFile && file.name.endsWith(".enc") } ?: return 0
            for (file in files) {
                if (file.lastModified() < cutoff) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return deletedCount
    }

    /**
     * Clear all evidence files upon database reset or manual purge.
     */
    open fun clearAllEvidence(): Int {
        var deletedCount = 0
        try {
            val files = evidenceDir.listFiles { file -> file.isFile && file.name.endsWith(".enc") } ?: return 0
            for (file in files) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return deletedCount
    }
}
