package com.aegisauth.feature.protectedapps;

import com.aegisauth.data.repository.ProtectedAppsRepository;
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
public final class ProtectedAppsViewModel_Factory implements Factory<ProtectedAppsViewModel> {
  private final Provider<ProtectedAppsRepository> repositoryProvider;

  public ProtectedAppsViewModel_Factory(Provider<ProtectedAppsRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ProtectedAppsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ProtectedAppsViewModel_Factory create(
      Provider<ProtectedAppsRepository> repositoryProvider) {
    return new ProtectedAppsViewModel_Factory(repositoryProvider);
  }

  public static ProtectedAppsViewModel newInstance(ProtectedAppsRepository repository) {
    return new ProtectedAppsViewModel(repository);
  }
}
