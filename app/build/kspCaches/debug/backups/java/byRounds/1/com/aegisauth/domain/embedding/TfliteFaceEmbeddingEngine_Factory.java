package com.aegisauth.domain.embedding;

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
public final class TfliteFaceEmbeddingEngine_Factory implements Factory<TfliteFaceEmbeddingEngine> {
  private final Provider<Context> contextProvider;

  public TfliteFaceEmbeddingEngine_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public TfliteFaceEmbeddingEngine get() {
    return newInstance(contextProvider.get());
  }

  public static TfliteFaceEmbeddingEngine_Factory create(Provider<Context> contextProvider) {
    return new TfliteFaceEmbeddingEngine_Factory(contextProvider);
  }

  public static TfliteFaceEmbeddingEngine newInstance(Context context) {
    return new TfliteFaceEmbeddingEngine(context);
  }
}
