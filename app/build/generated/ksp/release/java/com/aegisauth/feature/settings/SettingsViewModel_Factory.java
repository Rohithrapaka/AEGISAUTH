package com.aegisauth.feature.settings;

import com.aegisauth.data.datastore.AegisPreferences;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AegisPreferences> preferencesProvider;

  public SettingsViewModel_Factory(Provider<AegisPreferences> preferencesProvider) {
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(preferencesProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AegisPreferences> preferencesProvider) {
    return new SettingsViewModel_Factory(preferencesProvider);
  }

  public static SettingsViewModel newInstance(AegisPreferences preferences) {
    return new SettingsViewModel(preferences);
  }
}
