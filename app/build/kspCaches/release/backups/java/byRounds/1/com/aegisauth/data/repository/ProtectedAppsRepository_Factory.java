package com.aegisauth.data.repository;

import android.content.Context;
import com.aegisauth.data.local.dao.ProtectedAppDao;
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
public final class ProtectedAppsRepository_Factory implements Factory<ProtectedAppsRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<ProtectedAppDao> protectedAppDaoProvider;

  public ProtectedAppsRepository_Factory(Provider<Context> contextProvider,
      Provider<ProtectedAppDao> protectedAppDaoProvider) {
    this.contextProvider = contextProvider;
    this.protectedAppDaoProvider = protectedAppDaoProvider;
  }

  @Override
  public ProtectedAppsRepository get() {
    return newInstance(contextProvider.get(), protectedAppDaoProvider.get());
  }

  public static ProtectedAppsRepository_Factory create(Provider<Context> contextProvider,
      Provider<ProtectedAppDao> protectedAppDaoProvider) {
    return new ProtectedAppsRepository_Factory(contextProvider, protectedAppDaoProvider);
  }

  public static ProtectedAppsRepository newInstance(Context context,
      ProtectedAppDao protectedAppDao) {
    return new ProtectedAppsRepository(context, protectedAppDao);
  }
}
