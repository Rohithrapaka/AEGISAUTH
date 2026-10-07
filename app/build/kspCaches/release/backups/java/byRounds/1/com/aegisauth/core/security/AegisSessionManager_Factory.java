package com.aegisauth.core.security;

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
public final class AegisSessionManager_Factory implements Factory<AegisSessionManager> {
  @Override
  public AegisSessionManager get() {
    return newInstance();
  }

  public static AegisSessionManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AegisSessionManager newInstance() {
    return new AegisSessionManager();
  }

  private static final class InstanceHolder {
    private static final AegisSessionManager_Factory INSTANCE = new AegisSessionManager_Factory();
  }
}
