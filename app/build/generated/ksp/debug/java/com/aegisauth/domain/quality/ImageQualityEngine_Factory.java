package com.aegisauth.domain.quality;

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
public final class ImageQualityEngine_Factory implements Factory<ImageQualityEngine> {
  @Override
  public ImageQualityEngine get() {
    return newInstance();
  }

  public static ImageQualityEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImageQualityEngine newInstance() {
    return new ImageQualityEngine();
  }

  private static final class InstanceHolder {
    private static final ImageQualityEngine_Factory INSTANCE = new ImageQualityEngine_Factory();
  }
}
