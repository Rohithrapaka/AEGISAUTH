package com.aegisauth.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppProtectionStateRepository_Factory implements Factory<AppProtectionStateRepository> {
  @Override
  public AppProtectionStateRepository get() {
    return newInstance();
  }

  public static AppProtectionStateRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppProtectionStateRepository newInstance() {
    return new AppProtectionStateRepository();
  }

  private static final class InstanceHolder {
    private static final AppProtectionStateRepository_Factory INSTANCE = new AppProtectionStateRepository_Factory();
  }
}
