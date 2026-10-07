package com.aegisauth.feature.dashboard;

import android.content.Context;
import com.aegisauth.data.datastore.AegisPreferences;
import com.aegisauth.data.repository.AppProtectionStateRepository;
import com.aegisauth.data.repository.AuthenticationHistoryRepository;
import com.aegisauth.data.repository.BiometricTemplateRepository;
import com.aegisauth.data.repository.ProtectedAppsRepository;
import com.aegisauth.data.repository.SecurityAlertRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<AegisPreferences> preferencesProvider;

  private final Provider<BiometricTemplateRepository> biometricRepositoryProvider;

  private final Provider<ProtectedAppsRepository> protectedAppsRepositoryProvider;

  private final Provider<AuthenticationHistoryRepository> historyRepositoryProvider;

  private final Provider<SecurityAlertRepository> alertRepositoryProvider;

  private final Provider<AppProtectionStateRepository> appProtectionStateRepositoryProvider;

  public DashboardViewModel_Factory(Provider<Context> contextProvider,
      Provider<AegisPreferences> preferencesProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<ProtectedAppsRepository> protectedAppsRepositoryProvider,
      Provider<AuthenticationHistoryRepository> historyRepositoryProvider,
      Provider<SecurityAlertRepository> alertRepositoryProvider,
      Provider<AppProtectionStateRepository> appProtectionStateRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.preferencesProvider = preferencesProvider;
    this.biometricRepositoryProvider = biometricRepositoryProvider;
    this.protectedAppsRepositoryProvider = protectedAppsRepositoryProvider;
    this.historyRepositoryProvider = historyRepositoryProvider;
    this.alertRepositoryProvider = alertRepositoryProvider;
    this.appProtectionStateRepositoryProvider = appProtectionStateRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(contextProvider.get(), preferencesProvider.get(), biometricRepositoryProvider.get(), protectedAppsRepositoryProvider.get(), historyRepositoryProvider.get(), alertRepositoryProvider.get(), appProtectionStateRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<Context> contextProvider,
      Provider<AegisPreferences> preferencesProvider,
      Provider<BiometricTemplateRepository> biometricRepositoryProvider,
      Provider<ProtectedAppsRepository> protectedAppsRepositoryProvider,
      Provider<AuthenticationHistoryRepository> historyRepositoryProvider,
      Provider<SecurityAlertRepository> alertRepositoryProvider,
      Provider<AppProtectionStateRepository> appProtectionStateRepositoryProvider) {
    return new DashboardViewModel_Factory(contextProvider, preferencesProvider, biometricRepositoryProvider, protectedAppsRepositoryProvider, historyRepositoryProvider, alertRepositoryProvider, appProtectionStateRepositoryProvider);
  }

  public static DashboardViewModel newInstance(Context context, AegisPreferences preferences,
      BiometricTemplateRepository biometricRepository,
      ProtectedAppsRepository protectedAppsRepository,
      AuthenticationHistoryRepository historyRepository, SecurityAlertRepository alertRepository,
      AppProtectionStateRepository appProtectionStateRepository) {
    return new DashboardViewModel(context, preferences, biometricRepository, protectedAppsRepository, historyRepository, alertRepository, appProtectionStateRepository);
  }
}
