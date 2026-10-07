package com.aegisauth.core.di

import com.aegisauth.domain.biohash.BioHashEngine
import com.aegisauth.domain.biohash.OrthonormalBioHashEngine
import com.aegisauth.domain.challenge.ActiveChallengeEngine
import com.aegisauth.domain.challenge.ChallengeEngine
import com.aegisauth.domain.embedding.FaceEmbeddingEngine
import com.aegisauth.domain.embedding.OnDeviceFaceEmbeddingEngine
import com.aegisauth.domain.liveness.LivenessEngine
import com.aegisauth.domain.liveness.PassiveLivenessEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindFaceEmbeddingEngine(
        impl: OnDeviceFaceEmbeddingEngine
    ): FaceEmbeddingEngine

    @Binds
    @Singleton
    abstract fun bindBioHashEngine(
        impl: OrthonormalBioHashEngine
    ): BioHashEngine

    @Binds
    @Singleton
    abstract fun bindLivenessEngine(
        impl: PassiveLivenessEngine
    ): LivenessEngine

    @Binds
    @Singleton
    abstract fun bindChallengeEngine(
        impl: ActiveChallengeEngine
    ): ChallengeEngine
}
