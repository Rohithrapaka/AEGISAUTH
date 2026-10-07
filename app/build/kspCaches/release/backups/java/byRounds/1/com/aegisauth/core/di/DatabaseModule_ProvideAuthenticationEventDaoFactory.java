package com.aegisauth.core.di;

import com.aegisauth.data.local.AegisDatabase;
import com.aegisauth.data.local.dao.AuthenticationEventDao;
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
public final class DatabaseModule_ProvideAuthenticationEventDaoFactory implements Factory<AuthenticationEventDao> {
  private final Provider<AegisDatabase> databaseProvider;

  public DatabaseModule_ProvideAuthenticationEventDaoFactory(
      Provider<AegisDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AuthenticationEventDao get() {
    return provideAuthenticationEventDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideAuthenticationEventDaoFactory create(
      Provider<AegisDatabase> databaseProvider) {
    return new DatabaseModule_ProvideAuthenticationEventDaoFactory(databaseProvider);
  }

  public static AuthenticationEventDao provideAuthenticationEventDao(AegisDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAuthenticationEventDao(database));
  }
}
