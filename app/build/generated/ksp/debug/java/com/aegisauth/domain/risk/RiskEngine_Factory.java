package com.aegisauth.domain.risk;

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
public final class RiskEngine_Factory implements Factory<RiskEngine> {
  @Override
  public RiskEngine get() {
    return newInstance();
  }

  public static RiskEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RiskEngine newInstance() {
    return new RiskEngine();
  }

  private static final class InstanceHolder {
    private static final RiskEngine_Factory INSTANCE = new RiskEngine_Factory();
  }
}
