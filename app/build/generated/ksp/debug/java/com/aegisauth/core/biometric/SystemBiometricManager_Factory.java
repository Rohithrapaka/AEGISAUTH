package com.aegisauth.core.biometric;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SystemBiometricManager_Factory implements Factory<SystemBiometricManager> {
  private final Provider<Context> contextProvider;

  public SystemBiometricManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SystemBiometricManager get() {
    return newInstance(contextProvider.get());
  }

  public static SystemBiometricManager_Factory create(Provider<Context> contextProvider) {
    return new SystemBiometricManager_Factory(contextProvider);
  }

  public static SystemBiometricManager newInstance(Context context) {
    return new SystemBiometricManager(context);
  }
}
