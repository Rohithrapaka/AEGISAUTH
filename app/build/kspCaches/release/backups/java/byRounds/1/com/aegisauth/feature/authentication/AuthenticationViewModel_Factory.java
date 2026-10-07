package com.aegisauth.feature.authentication;

import com.aegisauth.core.audio.SoundFeedbackManager;
import com.aegisauth.core.biometric.SystemBiometricManager;
import com.aegisauth.core.security.AegisSessionManager;
import com.aegisauth.core.security.EvidenceManager;
import com.aegisauth.core.security.PasswordSecurityManager;
import com.aegisauth.core.security.PatternSecurityManager;
import com.aegisauth.data.datastore.AegisPreferences;
import com.aegisauth.data.repository.AuthenticationHistoryRepository;
import com.aegisauth.data.repository.BiometricTemplateRepository;
import com.aegisauth.data.repository.SecurityAlertRepository;
import com.aegisauth.domain.biohash.BioHashEngine;
import com.aegisauth.domain.challenge.ChallengeEngine;
import com.aegisauth.domain.embedding.FaceEmbeddingEngine;
import com.aegisauth.domain.liveness.LivenessEngine;
import com.aegisauth.domain.quality.ImageQualityEngine;
import com.aegisauth.domain.risk.RiskEngine;
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
public final class AuthenticationViewModel_Factory implements Factory<AuthenticationViewModel> {
  private final Provider<ImageQualityEngine> qualityEngineProvider;

  private final Provider<LivenessEngine> livenessEngineProvider;

  private final Provider<RiskEngine> riskEngineProvider;

  private final Provider<ChallengeEngine> challengeEngineProvider;

  private final Provider<FaceEmbeddingEngine> embeddingEngineProvider;

  private final Provider<BioHashEngine> bioHashEngineProvider;

  private final Provider<BiometricTemplateRepository> biometricRepositoryProvider;

  private final Provider<AuthenticationHistoryRepository> historyRepositoryProvider;

  private final Provider<SecurityAlertRepository> alertRepositoryProvider;

  private final Provider<EvidenceManager> evidenceManagerProvider;

  private final Provider<SoundFeedbackManager> soundFeedbackManagerProvider;

  private final Provider<SystemBiometricManager> systemBiometricManagerProvider;

  private final Provider<PatternSecurityManager> patternSecurityManagerProvider;

  private final Provider<PasswordSecurityManager> passwordSecurityManagerProvider;

  private final Provider<AegisSessionManager> aegisSessionManagerProvider;

  private final Provider<AegisPreferences> preferencesProvider;

  public AuthenticationViewModel_Factory(Provider<ImageQualityEngine> qualityEngineProvider,
      Provider<LivenessEngine> livenessEngineProvider, Provider<RiskEngine> riskEngineProvider,
      Provider<ChallengeEngine> challengeEngineProvider,
      Provider<FaceEmbeddingEngine> embeddingEngineProvider,
      Provider<BioHashEngine> bioHashEngineProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<AuthenticationHistoryRepository> historyRepositoryProvider,
      Provider<SecurityAlertRepository> alertRepositoryProvider,
      Provider<EvidenceManager> evidenceManagerProvider,
      Provider<SoundFeedbackManager> soundFeedbackManagerProvider,
      Provider<SystemBiometricManager> systemBiometricManagerProvider,
      Provider<PatternSecurityManager> patternSecurityManagerProvider,
      Provider<PasswordSecurityManager> passwordSecurityManagerProvider,
      Provider<AegisSessionManager> aegisSessionManagerProvider,
      Provider<AegisPreferences> preferencesProvider) {
    this.qualityEngineProvider = qualityEngineProvider;
    this.livenessEngineProvider = livenessEngineProvider;
    this.riskEngineProvider = riskEngineProvider;
    this.challengeEngineProvider = challengeEngineProvider;
    this.embeddingEngineProvider = embeddingEngineProvider;
    this.bioHashEngineProvider = bioHashEngineProvider;
    this.biometricRepositoryProvider = biometricRepositoryProvider;
    this.historyRepositoryProvider = historyRepositoryProvider;
    this.alertRepositoryProvider = alertRepositoryProvider;
    this.evidenceManagerProvider = evidenceManagerProvider;
    this.soundFeedbackManagerProvider = soundFeedbackManagerProvider;
    this.systemBiometricManagerProvider = systemBiometricManagerProvider;
    this.patternSecurityManagerProvider = patternSecurityManagerProvider;
    this.passwordSecurityManagerProvider = passwordSecurityManagerProvider;
    this.aegisSessionManagerProvider = aegisSessionManagerProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public AuthenticationViewModel get() {
    return newInstance(qualityEngineProvider.get(), livenessEngineProvider.get(), riskEngineProvider.get(), challengeEngineProvider.get(), embeddingEngineProvider.get(), bioHashEngineProvider.get(), biometricRepositoryProvider.get(), historyRepositoryProvider.get(), alertRepositoryProvider.get(), evidenceManagerProvider.get(), soundFeedbackManagerProvider.get(), systemBiometricManagerProvider.get(), patternSecurityManagerProvider.get(), passwordSecurityManagerProvider.get(), aegisSessionManagerProvider.get(), preferencesProvider.get());
  }

  public static AuthenticationViewModel_Factory create(
      Provider<ImageQualityEngine> qualityEngineProvider,
      Provider<LivenessEngine> livenessEngineProvider, Provider<RiskEngine> riskEngineProvider,
      Provider<ChallengeEngine> challengeEngineProvider,
      Provider<FaceEmbeddingEngine> embeddingEngineProvider,
      Provider<BioHashEngine> bioHashEngineProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<AuthenticationHistoryRepository> historyRepositoryProvider,
      Provider<SecurityAlertRepository> alertRepositoryProvider,
      Provider<EvidenceManager> evidenceManagerProvider,
      Provider<SoundFeedbackManager> soundFeedbackManagerProvider,
      Provider<SystemBiometricManager> systemBiometricManagerProvider,
      Provider<PatternSecurityManager> patternSecurityManagerProvider,
      Provider<PasswordSecurityManager> passwordSecurityManagerProvider,
      Provider<AegisSessionManager> aegisSessionManagerProvider,
      Provider<AegisPreferences> preferencesProvider) {
    return new AuthenticationViewModel_Factory(qualityEngineProvider, livenessEngineProvider, riskEngineProvider, challengeEngineProvider, embeddingEngineProvider, bioHashEngineProvider, biometricRepositoryProvider, historyRepositoryProvider, alertRepositoryProvider, evidenceManagerProvider, soundFeedbackManagerProvider, systemBiometricManagerProvider, patternSecurityManagerProvider, passwordSecurityManagerProvider, aegisSessionManagerProvider, preferencesProvider);
  }

  public static AuthenticationViewModel newInstance(ImageQualityEngine qualityEngine,
      LivenessEngine livenessEngine, RiskEngine riskEngine, ChallengeEngine challengeEngine,
      FaceEmbeddingEngine embeddingEngine, BioHashEngine bioHashEngine,
      BiometricTemplateRepository biometricRepository,
      AuthenticationHistoryRepository historyRepository, SecurityAlertRepository alertRepository,
      EvidenceManager evidenceManager, SoundFeedbackManager soundFeedbackManager,
      SystemBiometricManager systemBiometricManager, PatternSecurityManager patternSecurityManager,
      PasswordSecurityManager passwordSecurityManager, AegisSessionManager aegisSessionManager,
      AegisPreferences preferences) {
    return new AuthenticationViewModel(qualityEngine, livenessEngine, riskEngine, challengeEngine, embeddingEngine, bioHashEngine, biometricRepository, historyRepository, alertRepository, evidenceManager, soundFeedbackManager, systemBiometricManager, patternSecurityManager, passwordSecurityManager, aegisSessionManager, preferences);
  }
}
