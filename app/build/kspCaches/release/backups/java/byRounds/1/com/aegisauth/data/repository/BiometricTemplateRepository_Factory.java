package com.aegisauth.data.repository;

import android.content.Context;
import com.aegisauth.core.security.KeystoreManager;
import com.aegisauth.data.local.dao.BiometricStateDao;
import com.aegisauth.domain.biohash.BioHashEngine;
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
public final class BiometricTemplateRepository_Factory implements Factory<BiometricTemplateRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<KeystoreManager> keystoreManagerProvider;

  private final Provider<BioHashEngine> bioHashEngineProvider;

  private final Provider<BiometricStateDao> biometricStateDaoProvider;

  public BiometricTemplateRepository_Factory(Provider<Context> contextProvider,
      Provider<KeystoreManager> keystoreManagerProvider,
      Provider<BioHashEngine> bioHashEngineProvider,
      Provider<BiometricStateDao> biometricStateDaoProvider) {
    this.contextProvider = contextProvider;
    this.keystoreManagerProvider = keystoreManagerProvider;
    this.bioHashEngineProvider = bioHashEngineProvider;
    this.biometricStateDaoProvider = biometricStateDaoProvider;
  }

  @Override
  public BiometricTemplateRepository get() {
    return newInstance(contextProvider.get(), keystoreManagerProvider.get(), bioHashEngineProvider.get(), biometricStateDaoProvider.get());
  }

  public static BiometricTemplateRepository_Factory create(Provider<Context> contextProvider,
      Provider<KeystoreManager> keystoreManagerProvider,
      Provider<BioHashEngine> bioHashEngineProvider,
      Provider<BiometricStateDao> biometricStateDaoProvider) {
    return new BiometricTemplateRepository_Factory(contextProvider, keystoreManagerProvider, bioHashEngineProvider, biometricStateDaoProvider);
  }

  public static BiometricTemplateRepository newInstance(Context context,
      KeystoreManager keystoreManager, BioHashEngine bioHashEngine,
      BiometricStateDao biometricStateDao) {
    return new BiometricTemplateRepository(context, keystoreManager, bioHashEngine, biometricStateDao);
  }
}
