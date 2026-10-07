package com.aegisauth.data.repository;

import android.content.Context;
import com.aegisauth.data.local.dao.BiometricProfileDao;
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
public final class BiometricProfileRepository_Factory implements Factory<BiometricProfileRepository> {
  private final Provider<BiometricProfileDao> biometricProfileDaoProvider;

  private final Provider<Context> contextProvider;

  public BiometricProfileRepository_Factory(
      Provider<BiometricProfileDao> biometricProfileDaoProvider,
      Provider<Context> contextProvider) {
    this.biometricProfileDaoProvider = biometricProfileDaoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public BiometricProfileRepository get() {
    return newInstance(biometricProfileDaoProvider.get(), contextProvider.get());
  }

  public static BiometricProfileRepository_Factory create(
      Provider<BiometricProfileDao> biometricProfileDaoProvider,
      Provider<Context> contextProvider) {
    return new BiometricProfileRepository_Factory(biometricProfileDaoProvider, contextProvider);
  }

  public static BiometricProfileRepository newInstance(BiometricProfileDao biometricProfileDao,
      Context context) {
    return new BiometricProfileRepository(biometricProfileDao, context);
  }
}
