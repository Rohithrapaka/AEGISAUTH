package com.aegisauth.core.di;

import com.aegisauth.data.local.AegisDatabase;
import com.aegisauth.data.local.dao.BiometricProfileDao;
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
public final class DatabaseModule_ProvideBiometricProfileDaoFactory implements Factory<BiometricProfileDao> {
  private final Provider<AegisDatabase> databaseProvider;

  public DatabaseModule_ProvideBiometricProfileDaoFactory(
      Provider<AegisDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BiometricProfileDao get() {
    return provideBiometricProfileDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideBiometricProfileDaoFactory create(
      Provider<AegisDatabase> databaseProvider) {
    return new DatabaseModule_ProvideBiometricProfileDaoFactory(databaseProvider);
  }

  public static BiometricProfileDao provideBiometricProfileDao(AegisDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBiometricProfileDao(database));
  }
}
