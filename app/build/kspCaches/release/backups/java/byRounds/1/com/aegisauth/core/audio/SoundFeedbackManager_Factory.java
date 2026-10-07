package com.aegisauth.core.audio;

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
public final class SoundFeedbackManager_Factory implements Factory<SoundFeedbackManager> {
  @Override
  public SoundFeedbackManager get() {
    return newInstance();
  }

  public static SoundFeedbackManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SoundFeedbackManager newInstance() {
    return new SoundFeedbackManager();
  }

  private static final class InstanceHolder {
    private static final SoundFeedbackManager_Factory INSTANCE = new SoundFeedbackManager_Factory();
  }
}
