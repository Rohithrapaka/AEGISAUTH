package com.aegisauth.feature.alerts;

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
public final class AlertsViewModel_Factory implements Factory<AlertsViewModel> {
  private final Provider<SecurityAlertRepository> repositoryProvider;

  public AlertsViewModel_Factory(Provider<SecurityAlertRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AlertsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static AlertsViewModel_Factory create(
      Provider<SecurityAlertRepository> repositoryProvider) {
    return new AlertsViewModel_Factory(repositoryProvider);
  }

  public static AlertsViewModel newInstance(SecurityAlertRepository repository) {
    return new AlertsViewModel(repository);
  }
}
