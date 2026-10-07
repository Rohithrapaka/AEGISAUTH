package com.aegisauth.core.di;

import com.aegisauth.data.local.AegisDatabase;
import com.aegisauth.data.local.dao.BiometricStateDao;
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
public final class DatabaseModule_ProvideBiometricStateDaoFactory implements Factory<BiometricStateDao> {
  private final Provider<AegisDatabase> databaseProvider;

  public DatabaseModule_ProvideBiometricStateDaoFactory(Provider<AegisDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BiometricStateDao get() {
    return provideBiometricStateDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideBiometricStateDaoFactory create(
      Provider<AegisDatabase> databaseProvider) {
    return new DatabaseModule_ProvideBiometricStateDaoFactory(databaseProvider);
  }

  public static BiometricStateDao provideBiometricStateDao(AegisDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBiometricStateDao(database));
  }
}
