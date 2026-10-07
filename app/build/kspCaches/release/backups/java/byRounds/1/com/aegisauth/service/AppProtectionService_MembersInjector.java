package com.aegisauth.service;

import com.aegisauth.data.datastore.AegisPreferences;
import com.aegisauth.data.repository.AppProtectionStateRepository;
import com.aegisauth.data.repository.ProtectedAppsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AppProtectionService_MembersInjector implements MembersInjector<AppProtectionService> {
  private final Provider<ProtectedAppsRepository> protectedAppsRepositoryProvider;

  private final Provider<AegisPreferences> preferencesProvider;

  private final Provider<AppProtectionStateRepository> appProtectionStateRepositoryProvider;

  public AppProtectionService_MembersInjector(
      Provider<ProtectedAppsRepository> protectedAppsRepositoryProvider,
      Provider<AegisPreferences> preferencesProvider,
      Provider<AppProtectionStateRepository> appProtectionStateRepositoryProvider) {
    this.protectedAppsRepositoryProvider = protectedAppsRepositoryProvider;
    this.preferencesProvider = preferencesProvider;
    this.appProtectionStateRepositoryProvider = appProtectionStateRepositoryProvider;
  }

  public static MembersInjector<AppProtectionService> create(
      Provider<ProtectedAppsRepository> protectedAppsRepositoryProvider,
      Provider<AegisPreferences> preferencesProvider,
      Provider<AppProtectionStateRepository> appProtectionStateRepositoryProvider) {
    return new AppProtectionService_MembersInjector(protectedAppsRepositoryProvider, preferencesProvider, appProtectionStateRepositoryProvider);
  }

  @Override
  public void injectMembers(AppProtectionService instance) {
    injectProtectedAppsRepository(instance, protectedAppsRepositoryProvider.get());
    injectPreferences(instance, preferencesProvider.get());
    injectAppProtectionStateRepository(instance, appProtectionStateRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.aegisauth.service.AppProtectionService.protectedAppsRepository")
  public static void injectProtectedAppsRepository(AppProtectionService instance,
      ProtectedAppsRepository protectedAppsRepository) {
    instance.protectedAppsRepository = protectedAppsRepository;
  }

  @InjectedFieldSignature("com.aegisauth.service.AppProtectionService.preferences")
  public static void injectPreferences(AppProtectionService instance,
      AegisPreferences preferences) {
    instance.preferences = preferences;
  }

  @InjectedFieldSignature("com.aegisauth.service.AppProtectionService.appProtectionStateRepository")
  public static void injectAppProtectionStateRepository(AppProtectionService instance,
      AppProtectionStateRepository appProtectionStateRepository) {
    instance.appProtectionStateRepository = appProtectionStateRepository;
  }
}
