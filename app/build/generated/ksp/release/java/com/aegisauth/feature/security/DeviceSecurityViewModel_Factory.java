package com.aegisauth.feature.security;

import android.content.Context;
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
public final class DeviceSecurityViewModel_Factory implements Factory<DeviceSecurityViewModel> {
  private final Provider<Context> contextProvider;

  public DeviceSecurityViewModel_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DeviceSecurityViewModel get() {
    return newInstance(contextProvider.get());
  }

  public static DeviceSecurityViewModel_Factory create(Provider<Context> contextProvider) {
    return new DeviceSecurityViewModel_Factory(contextProvider);
  }

  public static DeviceSecurityViewModel newInstance(Context context) {
    return new DeviceSecurityViewModel(context);
  }
}
