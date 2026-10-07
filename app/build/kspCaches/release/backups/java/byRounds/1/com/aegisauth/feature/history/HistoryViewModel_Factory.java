package com.aegisauth.feature.history;

import com.aegisauth.core.security.EvidenceManager;
import com.aegisauth.data.repository.AuthenticationHistoryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class HistoryViewModel_Factory implements Factory<HistoryViewModel> {
  private final Provider<AuthenticationHistoryRepository> repositoryProvider;

  private final Provider<EvidenceManager> evidenceManagerProvider;

  public HistoryViewModel_Factory(Provider<AuthenticationHistoryRepository> repositoryProvider,
      Provider<EvidenceManager> evidenceManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.evidenceManagerProvider = evidenceManagerProvider;
  }

  @Override
  public HistoryViewModel get() {
    return newInstance(repositoryProvider.get(), evidenceManagerProvider.get());
  }

  public static HistoryViewModel_Factory create(
      Provider<AuthenticationHistoryRepository> repositoryProvider,
      Provider<EvidenceManager> evidenceManagerProvider) {
    return new HistoryViewModel_Factory(repositoryProvider, evidenceManagerProvider);
  }

  public static HistoryViewModel newInstance(AuthenticationHistoryRepository repository,
      EvidenceManager evidenceManager) {
    return new HistoryViewModel(repository, evidenceManager);
  }
}
