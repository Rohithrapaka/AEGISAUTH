package com.aegisauth.core.di;

import android.content.Context;
import com.aegisauth.data.local.AegisDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideAegisDatabaseFactory implements Factory<AegisDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideAegisDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AegisDatabase get() {
    return provideAegisDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideAegisDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideAegisDatabaseFactory(contextProvider);
  }

  public static AegisDatabase provideAegisDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAegisDatabase(context));
  }
}
