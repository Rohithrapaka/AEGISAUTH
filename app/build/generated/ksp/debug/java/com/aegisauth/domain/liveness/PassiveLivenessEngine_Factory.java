package com.aegisauth.domain.liveness;

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
public final class PassiveLivenessEngine_Factory implements Factory<PassiveLivenessEngine> {
  @Override
  public PassiveLivenessEngine get() {
    return newInstance();
  }

  public static PassiveLivenessEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PassiveLivenessEngine newInstance() {
    return new PassiveLivenessEngine();
  }

  private static final class InstanceHolder {
    private static final PassiveLivenessEngine_Factory INSTANCE = new PassiveLivenessEngine_Factory();
  }
}
