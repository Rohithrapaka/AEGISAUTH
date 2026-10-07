package com.aegisauth.core.di

import android.content.Context
import androidx.room.Room
import com.aegisauth.data.local.AegisDatabase
import com.aegisauth.data.local.dao.AuthenticationEventDao
import com.aegisauth.data.local.dao.BiometricProfileDao
import com.aegisauth.data.local.dao.BiometricStateDao
import com.aegisauth.data.local.dao.ProtectedAppDao
import com.aegisauth.data.local.dao.SecurityAlertDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAegisDatabase(
        @ApplicationContext context: Context
    ): AegisDatabase {
        return Room.databaseBuilder(
            context,
            AegisDatabase::class.java,
            "aegis_secure_db"
        )
            .addMigrations(AegisDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProtectedAppDao(database: AegisDatabase): ProtectedAppDao {
        return database.protectedAppDao()
    }

    @Provides
    fun provideAuthenticationEventDao(database: AegisDatabase): AuthenticationEventDao {
        return database.authenticationEventDao()
    }

    @Provides
    fun provideSecurityAlertDao(database: AegisDatabase): SecurityAlertDao {
        return database.securityAlertDao()
    }

    @Provides
    fun provideBiometricStateDao(database: AegisDatabase): BiometricStateDao {
        return database.biometricStateDao()
    }

    @Provides
    fun provideBiometricProfileDao(database: AegisDatabase): BiometricProfileDao {
        return database.biometricProfileDao()
    }
}
