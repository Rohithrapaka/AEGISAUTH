package com.aegisauth.core.di;

import com.aegisauth.data.local.AegisDatabase;
import com.aegisauth.data.local.dao.SecurityAlertDao;
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
public final class DatabaseModule_ProvideSecurityAlertDaoFactory implements Factory<SecurityAlertDao> {
  private final Provider<AegisDatabase> databaseProvider;

  public DatabaseModule_ProvideSecurityAlertDaoFactory(Provider<AegisDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SecurityAlertDao get() {
    return provideSecurityAlertDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSecurityAlertDaoFactory create(
      Provider<AegisDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSecurityAlertDaoFactory(databaseProvider);
  }

  public static SecurityAlertDao provideSecurityAlertDao(AegisDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSecurityAlertDao(database));
  }
}
