package com.aegisauth.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aegisauth.data.local.dao.AuthenticationEventDao
import com.aegisauth.data.local.dao.BiometricProfileDao
import com.aegisauth.data.local.dao.BiometricStateDao
import com.aegisauth.data.local.dao.ProtectedAppDao
import com.aegisauth.data.local.dao.SecurityAlertDao
import com.aegisauth.data.local.entity.AuthenticationEvent
import com.aegisauth.data.local.entity.BiometricProfile
import com.aegisauth.data.local.entity.BiometricState
import com.aegisauth.data.local.entity.ProtectedApplication
import com.aegisauth.data.local.entity.SecurityAlert

@Database(
    entities = [
        ProtectedApplication::class,
        AuthenticationEvent::class,
        SecurityAlert::class,
        BiometricState::class,
        BiometricProfile::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun protectedAppDao(): ProtectedAppDao
    abstract fun authenticationEventDao(): AuthenticationEventDao
    abstract fun securityAlertDao(): SecurityAlertDao
    abstract fun biometricStateDao(): BiometricStateDao
    abstract fun biometricProfileDao(): BiometricProfileDao

    companion object {
        /**
         * M15.1 — Safe non-destructive migration from DB v2 to v3.
         * 1. Creates biometric_profiles table.
         * 2. Adds profileId column to biometric_state (NOT NULL DEFAULT 0 = legacy BIO 1).
         * 3. Inserts the default "Default" profile with id=1.
         * 4. Updates existing biometric_state row to link to profileId=1.
         * The actual encrypted face template file (aegis_biohash_enc.bin) is NOT touched.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create biometric_profiles table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `biometric_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                // 2. Add profileId column to biometric_state
                db.execSQL(
                    "ALTER TABLE `biometric_state` ADD COLUMN `profileId` INTEGER NOT NULL DEFAULT 0"
                )
                // 3. Insert default BIO 1 profile
                val now = System.currentTimeMillis()
                db.execSQL(
                    "INSERT OR IGNORE INTO `biometric_profiles` (id, displayName, createdAt) VALUES (1, 'Default', $now)"
                )
                // 4. Link existing biometric_state row to BIO 1
                db.execSQL("UPDATE `biometric_state` SET `profileId` = 1 WHERE id = 1")
            }
        }
    }
}
