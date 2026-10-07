package com.aegisauth.domain.biohash;

import com.aegisauth.core.security.KeystoreManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class OrthonormalBioHashEngine_Factory implements Factory<OrthonormalBioHashEngine> {
  private final Provider<KeystoreManager> keystoreManagerProvider;

  public OrthonormalBioHashEngine_Factory(Provider<KeystoreManager> keystoreManagerProvider) {
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  @Override
  public OrthonormalBioHashEngine get() {
    return newInstance(keystoreManagerProvider.get());
  }

  public static OrthonormalBioHashEngine_Factory create(
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new OrthonormalBioHashEngine_Factory(keystoreManagerProvider);
  }

  public static OrthonormalBioHashEngine newInstance(KeystoreManager keystoreManager) {
    return new OrthonormalBioHashEngine(keystoreManager);
  }
}
