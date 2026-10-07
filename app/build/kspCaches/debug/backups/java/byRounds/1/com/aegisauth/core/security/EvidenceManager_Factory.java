package com.aegisauth.core.security;

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
public final class EvidenceManager_Factory implements Factory<EvidenceManager> {
  private final Provider<Context> contextProvider;

  private final Provider<KeystoreManager> keystoreManagerProvider;

  public EvidenceManager_Factory(Provider<Context> contextProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    this.contextProvider = contextProvider;
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  @Override
  public EvidenceManager get() {
    return newInstance(contextProvider.get(), keystoreManagerProvider.get());
  }

  public static EvidenceManager_Factory create(Provider<Context> contextProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new EvidenceManager_Factory(contextProvider, keystoreManagerProvider);
  }

  public static EvidenceManager newInstance(Context context, KeystoreManager keystoreManager) {
    return new EvidenceManager(context, keystoreManager);
  }
}
