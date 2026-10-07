package com.aegisauth.core.di;

import com.aegisauth.data.local.AegisDatabase;
import com.aegisauth.data.local.dao.ProtectedAppDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvideProtectedAppDaoFactory implements Factory<ProtectedAppDao> {
  private final Provider<AegisDatabase> databaseProvider;

  public DatabaseModule_ProvideProtectedAppDaoFactory(Provider<AegisDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ProtectedAppDao get() {
    return provideProtectedAppDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideProtectedAppDaoFactory create(
      Provider<AegisDatabase> databaseProvider) {
    return new DatabaseModule_ProvideProtectedAppDaoFactory(databaseProvider);
  }

  public static ProtectedAppDao provideProtectedAppDao(AegisDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideProtectedAppDao(database));
  }
}
