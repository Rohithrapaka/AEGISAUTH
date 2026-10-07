package com.aegisauth.data.repository

import android.content.Context
import com.aegisauth.core.security.KeystoreManager
import com.aegisauth.data.local.dao.BiometricStateDao
import com.aegisauth.data.local.entity.BiometricState
import com.aegisauth.domain.biohash.BioHashEngine
import com.aegisauth.domain.model.BioHashTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricTemplateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager,
    private val bioHashEngine: BioHashEngine,
    private val biometricStateDao: BiometricStateDao
) {
    private val templateFile = File(context.filesDir, "aegis_biohash_enc.bin")

    val isEnrolled: Flow<Boolean> = biometricStateDao.getBiometricState().map {
        it != null && it.isActive && templateFile.exists()
    }

    suspend fun hasEnrolledTemplate(): Boolean = withContext(Dispatchers.IO) {
        val state = biometricStateDao.getBiometricStateSync()
        state != null && state.isActive && templateFile.exists()
    }

    companion object {
        private const val MAGIC_MULTI = 0x41454753 // "AEGS"
    }

    suspend fun saveTemplates(templates: List<BioHashTemplate>): Boolean = withContext(Dispatchers.IO) {
        if (templates.isEmpty()) return@withContext false
        try {
            // 1. Serialize multi-template container
            val byteStream = java.io.ByteArrayOutputStream()
            val dataOut = java.io.DataOutputStream(byteStream)
            dataOut.writeInt(MAGIC_MULTI)
            dataOut.writeInt(templates.size)
            for (template in templates) {
                val rawBytes = bioHashEngine.serializeTemplateToBytes(template)
                dataOut.writeInt(rawBytes.size)
                dataOut.write(rawBytes)
            }
            dataOut.flush()

            val rawContainerBytes = byteStream.toByteArray()

            // 2. Encrypt using Android Keystore AES-256-GCM
            val encryptedBytes = keystoreManager.encrypt(rawContainerBytes)

            // 3. Write encrypted template file to private internal storage
            templateFile.writeBytes(encryptedBytes)

            // 4. Compute non-reversible SHA-256 hash of primary template ID
            val primaryTemplate = templates.first()
            val md = MessageDigest.getInstance("SHA-256")
            val hashBytes = md.digest(primaryTemplate.id.toByteArray())
            val templateHash = hashBytes.joinToString("") { "%02x".format(it) }

            // 5. Update Room database state
            val state = BiometricState(
                id = 1,
                enrollmentVersion = primaryTemplate.version,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isActive = true,
                samplesCount = templates.size,
                templateHash = templateHash
            )
            biometricStateDao.insertOrUpdate(state)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveTemplate(template: BioHashTemplate): Boolean = saveTemplates(listOf(template))

    suspend fun loadAllTemplates(): List<BioHashTemplate> = withContext(Dispatchers.IO) {
        try {
            if (!templateFile.exists()) return@withContext emptyList()

            val encryptedBytes = templateFile.readBytes()
            val decryptedBytes = keystoreManager.decrypt(encryptedBytes)

            if (decryptedBytes.size >= 8) {
                val dataIn = java.io.DataInputStream(java.io.ByteArrayInputStream(decryptedBytes))
                val magic = dataIn.readInt()
                if (magic == MAGIC_MULTI) {
                    val count = dataIn.readInt()
                    val list = ArrayList<BioHashTemplate>(count)
                    for (i in 0 until count) {
                        val len = dataIn.readInt()
                        val bytes = ByteArray(len)
                        dataIn.readFully(bytes)
                        list.add(bioHashEngine.deserializeTemplateFromBytes(bytes))
                    }
                    return@withContext list
                }
            }

            // Legacy single template format fallback
            val single = bioHashEngine.deserializeTemplateFromBytes(decryptedBytes)
            listOf(single)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun loadTemplate(): BioHashTemplate? = withContext(Dispatchers.IO) {
        loadAllTemplates().firstOrNull()
    }

    suspend fun deleteTemplate(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (templateFile.exists()) {
                templateFile.delete()
            }
            biometricStateDao.deleteState()
            keystoreManager.wipeBiometricKeys()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
