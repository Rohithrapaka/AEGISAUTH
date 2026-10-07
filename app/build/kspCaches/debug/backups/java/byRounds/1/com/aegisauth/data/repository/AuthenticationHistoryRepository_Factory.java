package com.aegisauth.data.repository;

import com.aegisauth.data.local.dao.AuthenticationEventDao;
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
public final class AuthenticationHistoryRepository_Factory implements Factory<AuthenticationHistoryRepository> {
  private final Provider<AuthenticationEventDao> eventDaoProvider;

  public AuthenticationHistoryRepository_Factory(
      Provider<AuthenticationEventDao> eventDaoProvider) {
    this.eventDaoProvider = eventDaoProvider;
  }

  @Override
  public AuthenticationHistoryRepository get() {
    return newInstance(eventDaoProvider.get());
  }

  public static AuthenticationHistoryRepository_Factory create(
      Provider<AuthenticationEventDao> eventDaoProvider) {
    return new AuthenticationHistoryRepository_Factory(eventDaoProvider);
  }

  public static AuthenticationHistoryRepository newInstance(AuthenticationEventDao eventDao) {
    return new AuthenticationHistoryRepository(eventDao);
  }
}
