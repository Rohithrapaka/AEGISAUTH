package com.aegisauth.domain.challenge;

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
public final class ActiveChallengeEngine_Factory implements Factory<ActiveChallengeEngine> {
  @Override
  public ActiveChallengeEngine get() {
    return newInstance();
  }

  public static ActiveChallengeEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ActiveChallengeEngine newInstance() {
    return new ActiveChallengeEngine();
  }

  private static final class InstanceHolder {
    private static final ActiveChallengeEngine_Factory INSTANCE = new ActiveChallengeEngine_Factory();
  }
}
