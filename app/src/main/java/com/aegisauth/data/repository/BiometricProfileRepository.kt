package com.aegisauth.data.repository

import android.content.Context
import com.aegisauth.data.local.dao.BiometricProfileDao
import com.aegisauth.data.local.entity.BiometricProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M15.1 — Manages multi-biometric profile metadata and their associated encrypted template files.
 *
 * Template file naming:
 *   BIO 1 (legacy): aegis_biohash_enc.bin  (backward compat with pre-M15.1 installs)
 *   BIO N (N > 1):  aegis_biohash_enc_<profileId>.bin
 *
 * Security invariants:
 *   - Max 5 profiles enforced BOTH in repository AND UI layer.
 *   - Profile display names are UI metadata only — never embedded in cryptographic keys or BioHash.
 *   - Deleting a profile deletes ONLY that profile's template file; others are never touched.
 *   - No biometric data (embeddings, BioHash bits) are stored here — only file references.
 */
@Singleton
class BiometricProfileRepository @Inject constructor(
    private val biometricProfileDao: BiometricProfileDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MAX_PROFILES = 5
        const val LEGACY_TEMPLATE_FILE = "aegis_biohash_enc.bin"

        fun templateFilenameForProfile(profileId: Long): String {
            return if (profileId == 1L) LEGACY_TEMPLATE_FILE
            else "aegis_biohash_enc_$profileId.bin"
        }
    }

    fun getAllProfiles(): Flow<List<BiometricProfile>> = biometricProfileDao.getAllProfiles()

    suspend fun getProfileById(profileId: Long): BiometricProfile? =
        biometricProfileDao.getProfileById(profileId)

    suspend fun getProfileCount(): Int = biometricProfileDao.getProfileCount()

    /**
     * M15.2 — Ensures BIO 1 (Default) exists whenever an enrolled face template exists or on fresh start.
     */
    suspend fun ensureDefaultProfile(): Long {
        val count = biometricProfileDao.getProfileCount()
        if (count == 0) {
            return biometricProfileDao.insertProfile(
                BiometricProfile(displayName = "Default")
            )
        }
        return 1L
    }

    /**
     * Insert a new profile enforcing MAX_PROFILES.
     * @return the new profile id, or -1 if limit reached.
     */
    suspend fun insertProfile(displayName: String): Long {
        if (biometricProfileDao.getProfileCount() >= MAX_PROFILES) return -1L
        return biometricProfileDao.insertProfile(
            BiometricProfile(displayName = displayName.trim().ifBlank { "BIO" })
        )
    }

    suspend fun renameProfile(profileId: Long, newName: String) {
        val existing = biometricProfileDao.getProfileById(profileId) ?: return
        biometricProfileDao.updateProfile(existing.copy(displayName = newName.trim().ifBlank { existing.displayName }))
    }

    /**
     * Delete a profile and its associated encrypted template file.
     * Only the file for this specific profileId is deleted.
     */
    suspend fun deleteProfile(profileId: Long) {
        biometricProfileDao.deleteProfile(profileId)
        val file = getTemplateFile(profileId)
        if (file.exists()) file.delete()
    }

    fun getTemplateFile(profileId: Long): File =
        File(context.filesDir, templateFilenameForProfile(profileId))

    fun hasTemplateFile(profileId: Long): Boolean = getTemplateFile(profileId).exists()
}
