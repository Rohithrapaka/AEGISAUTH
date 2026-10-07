package com.aegisauth.data.repository;

import com.aegisauth.data.local.dao.SecurityAlertDao;
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
public final class SecurityAlertRepository_Factory implements Factory<SecurityAlertRepository> {
  private final Provider<SecurityAlertDao> alertDaoProvider;

  public SecurityAlertRepository_Factory(Provider<SecurityAlertDao> alertDaoProvider) {
    this.alertDaoProvider = alertDaoProvider;
  }

  @Override
  public SecurityAlertRepository get() {
    return newInstance(alertDaoProvider.get());
  }

  public static SecurityAlertRepository_Factory create(
      Provider<SecurityAlertDao> alertDaoProvider) {
    return new SecurityAlertRepository_Factory(alertDaoProvider);
  }

  public static SecurityAlertRepository newInstance(SecurityAlertDao alertDao) {
    return new SecurityAlertRepository(alertDao);
  }
}
