package com.aegisauth.feature.enrollment;

import com.aegisauth.core.audio.SoundFeedbackManager;
import com.aegisauth.data.repository.BiometricTemplateRepository;
import com.aegisauth.domain.biohash.BioHashEngine;
import com.aegisauth.domain.embedding.FaceEmbeddingEngine;
import com.aegisauth.domain.quality.ImageQualityEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class EnrollmentViewModel_Factory implements Factory<EnrollmentViewModel> {
  private final Provider<ImageQualityEngine> qualityEngineProvider;

  private final Provider<FaceEmbeddingEngine> embeddingEngineProvider;

  private final Provider<BioHashEngine> bioHashEngineProvider;

  private final Provider<BiometricTemplateRepository> biometricRepositoryProvider;

  private final Provider<SoundFeedbackManager> soundFeedbackManagerProvider;

  public EnrollmentViewModel_Factory(Provider<ImageQualityEngine> qualityEngineProvider,
      Provider<FaceEmbeddingEngine> embeddingEngineProvider,
      Provider<BioHashEngine> bioHashEngineProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<SoundFeedbackManager> soundFeedbackManagerProvider) {
    this.qualityEngineProvider = qualityEngineProvider;
    this.embeddingEngineProvider = embeddingEngineProvider;
    this.bioHashEngineProvider = bioHashEngineProvider;
    this.biometricRepositoryProvider = biometricRepositoryProvider;
    this.soundFeedbackManagerProvider = soundFeedbackManagerProvider;
  }

  @Override
  public EnrollmentViewModel get() {
    return newInstance(qualityEngineProvider.get(), embeddingEngineProvider.get(), bioHashEngineProvider.get(), biometricRepositoryProvider.get(), soundFeedbackManagerProvider.get());
  }

  public static EnrollmentViewModel_Factory create(
      Provider<ImageQualityEngine> qualityEngineProvider,
      Provider<FaceEmbeddingEngine> embeddingEngineProvider,
      Provider<BioHashEngine> bioHashEngineProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<SoundFeedbackManager> soundFeedbackManagerProvider) {
    return new EnrollmentViewModel_Factory(qualityEngineProvider, embeddingEngineProvider, bioHashEngineProvider, biometricRepositoryProvider, soundFeedbackManagerProvider);
  }

  public static EnrollmentViewModel newInstance(ImageQualityEngine qualityEngine,
      FaceEmbeddingEngine embeddingEngine, BioHashEngine bioHashEngine,
      BiometricTemplateRepository biometricRepository, SoundFeedbackManager soundFeedbackManager) {
    return new EnrollmentViewModel(qualityEngine, embeddingEngine, bioHashEngine, biometricRepository, soundFeedbackManager);
  }
}
