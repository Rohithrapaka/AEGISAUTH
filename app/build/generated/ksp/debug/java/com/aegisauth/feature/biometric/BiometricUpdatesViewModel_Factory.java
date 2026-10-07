package com.aegisauth.feature.biometric;

import com.aegisauth.core.biometric.SystemBiometricManager;
import com.aegisauth.data.local.dao.BiometricStateDao;
import com.aegisauth.data.repository.BiometricProfileRepository;
import com.aegisauth.data.repository.BiometricTemplateRepository;
import com.aegisauth.data.repository.SecurityAlertRepository;
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
public final class BiometricUpdatesViewModel_Factory implements Factory<BiometricUpdatesViewModel> {
  private final Provider<BiometricTemplateRepository> repositoryProvider;

  private final Provider<BiometricStateDao> biometricStateDaoProvider;

  private final Provider<SecurityAlertRepository> alertRepositoryProvider;

  private final Provider<BiometricProfileRepository> profileRepositoryProvider;

  private final Provider<SystemBiometricManager> systemBiometricManagerProvider;

  public BiometricUpdatesViewModel_Factory(Provider<BiometricTemplateRepository> repositoryProvider,
      Provider<BiometricStateDao> biometricStateDaoProvider,
      Provider<SecurityAlertRepository> alertRepositoryProvider,
      Provider<BiometricProfileRepository> profileRepositoryProvider,
      Provider<SystemBiometricManager> systemBiometricManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.biometricStateDaoProvider = biometricStateDaoProvider;
    this.alertRepositoryProvider = alertRepositoryProvider;
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.systemBiometricManagerProvider = systemBiometricManagerProvider;
  }

  @Override
  public BiometricUpdatesViewModel get() {
    return newInstance(repositoryProvider.get(), biometricStateDaoProvider.get(), alertRepositoryProvider.get(), profileRepositoryProvider.get(), systemBiometricManagerProvider.get());
  }

  public static BiometricUpdatesViewModel_Factory create(
      Provider<BiometricTemplateRepository> repositoryProvider,
      Provider<BiometricStateDao> biometricStateDaoProvider,
      Provider<SecurityAlertRepository> alertRepositoryProvider,
      Provider<BiometricProfileRepository> profileRepositoryProvider,
      Provider<SystemBiometricManager> systemBiometricManagerProvider) {
    return new BiometricUpdatesViewModel_Factory(repositoryProvider, biometricStateDaoProvider, alertRepositoryProvider, profileRepositoryProvider, systemBiometricManagerProvider);
  }

  public static BiometricUpdatesViewModel newInstance(BiometricTemplateRepository repository,
      BiometricStateDao biometricStateDao, SecurityAlertRepository alertRepository,
      BiometricProfileRepository profileRepository, SystemBiometricManager systemBiometricManager) {
    return new BiometricUpdatesViewModel(repository, biometricStateDao, alertRepository, profileRepository, systemBiometricManager);
  }
}
