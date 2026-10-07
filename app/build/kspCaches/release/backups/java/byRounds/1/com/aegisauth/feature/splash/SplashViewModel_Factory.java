package com.aegisauth.feature.splash;

import com.aegisauth.core.security.AegisSessionManager;
import com.aegisauth.data.datastore.AegisPreferences;
import com.aegisauth.data.repository.BiometricTemplateRepository;
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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<AegisPreferences> preferencesProvider;

  private final Provider<BiometricTemplateRepository> biometricRepositoryProvider;

  private final Provider<AegisSessionManager> sessionManagerProvider;

  public SplashViewModel_Factory(Provider<AegisPreferences> preferencesProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<AegisSessionManager> sessionManagerProvider) {
    this.preferencesProvider = preferencesProvider;
    this.biometricRepositoryProvider = biometricRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(preferencesProvider.get(), biometricRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static SplashViewModel_Factory create(Provider<AegisPreferences> preferencesProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<AegisSessionManager> sessionManagerProvider) {
    return new SplashViewModel_Factory(preferencesProvider, biometricRepositoryProvider, sessionManagerProvider);
  }

  public static SplashViewModel newInstance(AegisPreferences preferences,
      BiometricTemplateRepository biometricRepository, AegisSessionManager sessionManager) {
    return new SplashViewModel(preferences, biometricRepository, sessionManager);
  }
}
