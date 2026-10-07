package com.aegisauth;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.aegisauth.core.audio.SoundFeedbackManager;
import com.aegisauth.core.biometric.SystemBiometricManager;
import com.aegisauth.core.di.DatabaseModule_ProvideAegisDatabaseFactory;
import com.aegisauth.core.di.DatabaseModule_ProvideAuthenticationEventDaoFactory;
import com.aegisauth.core.di.DatabaseModule_ProvideBiometricProfileDaoFactory;
import com.aegisauth.core.di.DatabaseModule_ProvideBiometricStateDaoFactory;
import com.aegisauth.core.di.DatabaseModule_ProvideProtectedAppDaoFactory;
import com.aegisauth.core.di.DatabaseModule_ProvideSecurityAlertDaoFactory;
import com.aegisauth.core.security.AegisSessionManager;
import com.aegisauth.core.security.EvidenceManager;
import com.aegisauth.core.security.KeystoreManager;
import com.aegisauth.core.security.PasswordSecurityManager;
import com.aegisauth.core.security.PatternSecurityManager;
import com.aegisauth.data.datastore.AegisPreferences;
import com.aegisauth.data.local.AegisDatabase;
import com.aegisauth.data.local.dao.AuthenticationEventDao;
import com.aegisauth.data.local.dao.BiometricProfileDao;
import com.aegisauth.data.local.dao.BiometricStateDao;
import com.aegisauth.data.local.dao.ProtectedAppDao;
import com.aegisauth.data.local.dao.SecurityAlertDao;
import com.aegisauth.data.repository.AppProtectionStateRepository;
import com.aegisauth.data.repository.AuthenticationHistoryRepository;
import com.aegisauth.data.repository.BiometricProfileRepository;
import com.aegisauth.data.repository.BiometricTemplateRepository;
import com.aegisauth.data.repository.ProtectedAppsRepository;
import com.aegisauth.data.repository.SecurityAlertRepository;
import com.aegisauth.domain.biohash.OrthonormalBioHashEngine;
import com.aegisauth.domain.challenge.ActiveChallengeEngine;
import com.aegisauth.domain.embedding.OnDeviceFaceEmbeddingEngine;
import com.aegisauth.domain.liveness.PassiveLivenessEngine;
import com.aegisauth.domain.quality.ImageQualityEngine;
import com.aegisauth.domain.risk.RiskEngine;
import com.aegisauth.feature.alerts.AlertsViewModel;
import com.aegisauth.feature.alerts.AlertsViewModel_HiltModules;
import com.aegisauth.feature.authentication.AuthOverlayActivity;
import com.aegisauth.feature.authentication.AuthenticationViewModel;
import com.aegisauth.feature.authentication.AuthenticationViewModel_HiltModules;
import com.aegisauth.feature.biometric.BiometricUpdatesViewModel;
import com.aegisauth.feature.biometric.BiometricUpdatesViewModel_HiltModules;
import com.aegisauth.feature.dashboard.DashboardViewModel;
import com.aegisauth.feature.dashboard.DashboardViewModel_HiltModules;
import com.aegisauth.feature.enrollment.EnrollmentViewModel;
import com.aegisauth.feature.enrollment.EnrollmentViewModel_HiltModules;
import com.aegisauth.feature.history.HistoryViewModel;
import com.aegisauth.feature.history.HistoryViewModel_HiltModules;
import com.aegisauth.feature.onboarding.OnboardingViewModel;
import com.aegisauth.feature.onboarding.OnboardingViewModel_HiltModules;
import com.aegisauth.feature.protectedapps.ProtectedAppsViewModel;
import com.aegisauth.feature.protectedapps.ProtectedAppsViewModel_HiltModules;
import com.aegisauth.feature.security.DeviceSecurityViewModel;
import com.aegisauth.feature.security.DeviceSecurityViewModel_HiltModules;
import com.aegisauth.feature.settings.SettingsViewModel;
import com.aegisauth.feature.settings.SettingsViewModel_HiltModules;
import com.aegisauth.feature.splash.SplashViewModel;
import com.aegisauth.feature.splash.SplashViewModel_HiltModules;
import com.aegisauth.service.AppProtectionService;
import com.aegisauth.service.AppProtectionService_MembersInjector;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerAegisApp_HiltComponents_SingletonC {
  private DaggerAegisApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public AegisApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements AegisApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public AegisApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements AegisApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public AegisApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements AegisApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public AegisApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements AegisApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public AegisApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements AegisApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public AegisApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements AegisApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public AegisApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements AegisApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public AegisApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends AegisApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends AegisApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends AegisApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends AegisApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public void injectAuthOverlayActivity(AuthOverlayActivity authOverlayActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(11).put(LazyClassKeyProvider.com_aegisauth_feature_alerts_AlertsViewModel, AlertsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_authentication_AuthenticationViewModel, AuthenticationViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_biometric_BiometricUpdatesViewModel, BiometricUpdatesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_dashboard_DashboardViewModel, DashboardViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_security_DeviceSecurityViewModel, DeviceSecurityViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_enrollment_EnrollmentViewModel, EnrollmentViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_history_HistoryViewModel, HistoryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_onboarding_OnboardingViewModel, OnboardingViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_protectedapps_ProtectedAppsViewModel, ProtectedAppsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_aegisauth_feature_splash_SplashViewModel, SplashViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_aegisauth_feature_enrollment_EnrollmentViewModel = "com.aegisauth.feature.enrollment.EnrollmentViewModel";

      static String com_aegisauth_feature_splash_SplashViewModel = "com.aegisauth.feature.splash.SplashViewModel";

      static String com_aegisauth_feature_history_HistoryViewModel = "com.aegisauth.feature.history.HistoryViewModel";

      static String com_aegisauth_feature_onboarding_OnboardingViewModel = "com.aegisauth.feature.onboarding.OnboardingViewModel";

      static String com_aegisauth_feature_dashboard_DashboardViewModel = "com.aegisauth.feature.dashboard.DashboardViewModel";

      static String com_aegisauth_feature_biometric_BiometricUpdatesViewModel = "com.aegisauth.feature.biometric.BiometricUpdatesViewModel";

      static String com_aegisauth_feature_protectedapps_ProtectedAppsViewModel = "com.aegisauth.feature.protectedapps.ProtectedAppsViewModel";

      static String com_aegisauth_feature_security_DeviceSecurityViewModel = "com.aegisauth.feature.security.DeviceSecurityViewModel";

      static String com_aegisauth_feature_settings_SettingsViewModel = "com.aegisauth.feature.settings.SettingsViewModel";

      static String com_aegisauth_feature_alerts_AlertsViewModel = "com.aegisauth.feature.alerts.AlertsViewModel";

      static String com_aegisauth_feature_authentication_AuthenticationViewModel = "com.aegisauth.feature.authentication.AuthenticationViewModel";

      @KeepFieldType
      EnrollmentViewModel com_aegisauth_feature_enrollment_EnrollmentViewModel2;

      @KeepFieldType
      SplashViewModel com_aegisauth_feature_splash_SplashViewModel2;

      @KeepFieldType
      HistoryViewModel com_aegisauth_feature_history_HistoryViewModel2;

      @KeepFieldType
      OnboardingViewModel com_aegisauth_feature_onboarding_OnboardingViewModel2;

      @KeepFieldType
      DashboardViewModel com_aegisauth_feature_dashboard_DashboardViewModel2;

      @KeepFieldType
      BiometricUpdatesViewModel com_aegisauth_feature_biometric_BiometricUpdatesViewModel2;

      @KeepFieldType
      ProtectedAppsViewModel com_aegisauth_feature_protectedapps_ProtectedAppsViewModel2;

      @KeepFieldType
      DeviceSecurityViewModel com_aegisauth_feature_security_DeviceSecurityViewModel2;

      @KeepFieldType
      SettingsViewModel com_aegisauth_feature_settings_SettingsViewModel2;

      @KeepFieldType
      AlertsViewModel com_aegisauth_feature_alerts_AlertsViewModel2;

      @KeepFieldType
      AuthenticationViewModel com_aegisauth_feature_authentication_AuthenticationViewModel2;
    }
  }

  private static final class ViewModelCImpl extends AegisApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AlertsViewModel> alertsViewModelProvider;

    private Provider<AuthenticationViewModel> authenticationViewModelProvider;

    private Provider<BiometricUpdatesViewModel> biometricUpdatesViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<DeviceSecurityViewModel> deviceSecurityViewModelProvider;

    private Provider<EnrollmentViewModel> enrollmentViewModelProvider;

    private Provider<HistoryViewModel> historyViewModelProvider;

    private Provider<OnboardingViewModel> onboardingViewModelProvider;

    private Provider<ProtectedAppsViewModel> protectedAppsViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SplashViewModel> splashViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.alertsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.authenticationViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.biometricUpdatesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.deviceSecurityViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.enrollmentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.historyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.onboardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.protectedAppsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.splashViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(11).put(LazyClassKeyProvider.com_aegisauth_feature_alerts_AlertsViewModel, ((Provider) alertsViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_authentication_AuthenticationViewModel, ((Provider) authenticationViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_biometric_BiometricUpdatesViewModel, ((Provider) biometricUpdatesViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_dashboard_DashboardViewModel, ((Provider) dashboardViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_security_DeviceSecurityViewModel, ((Provider) deviceSecurityViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_enrollment_EnrollmentViewModel, ((Provider) enrollmentViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_history_HistoryViewModel, ((Provider) historyViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_onboarding_OnboardingViewModel, ((Provider) onboardingViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_protectedapps_ProtectedAppsViewModel, ((Provider) protectedAppsViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_aegisauth_feature_splash_SplashViewModel, ((Provider) splashViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_aegisauth_feature_protectedapps_ProtectedAppsViewModel = "com.aegisauth.feature.protectedapps.ProtectedAppsViewModel";

      static String com_aegisauth_feature_history_HistoryViewModel = "com.aegisauth.feature.history.HistoryViewModel";

      static String com_aegisauth_feature_onboarding_OnboardingViewModel = "com.aegisauth.feature.onboarding.OnboardingViewModel";

      static String com_aegisauth_feature_splash_SplashViewModel = "com.aegisauth.feature.splash.SplashViewModel";

      static String com_aegisauth_feature_authentication_AuthenticationViewModel = "com.aegisauth.feature.authentication.AuthenticationViewModel";

      static String com_aegisauth_feature_dashboard_DashboardViewModel = "com.aegisauth.feature.dashboard.DashboardViewModel";

      static String com_aegisauth_feature_biometric_BiometricUpdatesViewModel = "com.aegisauth.feature.biometric.BiometricUpdatesViewModel";

      static String com_aegisauth_feature_enrollment_EnrollmentViewModel = "com.aegisauth.feature.enrollment.EnrollmentViewModel";

      static String com_aegisauth_feature_settings_SettingsViewModel = "com.aegisauth.feature.settings.SettingsViewModel";

      static String com_aegisauth_feature_security_DeviceSecurityViewModel = "com.aegisauth.feature.security.DeviceSecurityViewModel";

      static String com_aegisauth_feature_alerts_AlertsViewModel = "com.aegisauth.feature.alerts.AlertsViewModel";

      @KeepFieldType
      ProtectedAppsViewModel com_aegisauth_feature_protectedapps_ProtectedAppsViewModel2;

      @KeepFieldType
      HistoryViewModel com_aegisauth_feature_history_HistoryViewModel2;

      @KeepFieldType
      OnboardingViewModel com_aegisauth_feature_onboarding_OnboardingViewModel2;

      @KeepFieldType
      SplashViewModel com_aegisauth_feature_splash_SplashViewModel2;

      @KeepFieldType
      AuthenticationViewModel com_aegisauth_feature_authentication_AuthenticationViewModel2;

      @KeepFieldType
      DashboardViewModel com_aegisauth_feature_dashboard_DashboardViewModel2;

      @KeepFieldType
      BiometricUpdatesViewModel com_aegisauth_feature_biometric_BiometricUpdatesViewModel2;

      @KeepFieldType
      EnrollmentViewModel com_aegisauth_feature_enrollment_EnrollmentViewModel2;

      @KeepFieldType
      SettingsViewModel com_aegisauth_feature_settings_SettingsViewModel2;

      @KeepFieldType
      DeviceSecurityViewModel com_aegisauth_feature_security_DeviceSecurityViewModel2;

      @KeepFieldType
      AlertsViewModel com_aegisauth_feature_alerts_AlertsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.aegisauth.feature.alerts.AlertsViewModel 
          return (T) new AlertsViewModel(singletonCImpl.securityAlertRepositoryProvider.get());

          case 1: // com.aegisauth.feature.authentication.AuthenticationViewModel 
          return (T) new AuthenticationViewModel(singletonCImpl.imageQualityEngineProvider.get(), singletonCImpl.passiveLivenessEngineProvider.get(), singletonCImpl.riskEngineProvider.get(), singletonCImpl.activeChallengeEngineProvider.get(), singletonCImpl.onDeviceFaceEmbeddingEngineProvider.get(), singletonCImpl.orthonormalBioHashEngineProvider.get(), singletonCImpl.biometricTemplateRepositoryProvider.get(), singletonCImpl.authenticationHistoryRepositoryProvider.get(), singletonCImpl.securityAlertRepositoryProvider.get(), singletonCImpl.evidenceManagerProvider.get(), singletonCImpl.soundFeedbackManagerProvider.get(), singletonCImpl.systemBiometricManagerProvider.get(), singletonCImpl.patternSecurityManagerProvider.get(), singletonCImpl.passwordSecurityManagerProvider.get(), singletonCImpl.aegisSessionManagerProvider.get(), singletonCImpl.aegisPreferencesProvider.get());

          case 2: // com.aegisauth.feature.biometric.BiometricUpdatesViewModel 
          return (T) new BiometricUpdatesViewModel(singletonCImpl.biometricTemplateRepositoryProvider.get(), singletonCImpl.biometricStateDao(), singletonCImpl.securityAlertRepositoryProvider.get(), singletonCImpl.biometricProfileRepositoryProvider.get());

          case 3: // com.aegisauth.feature.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.aegisPreferencesProvider.get(), singletonCImpl.biometricTemplateRepositoryProvider.get(), singletonCImpl.protectedAppsRepositoryProvider.get(), singletonCImpl.authenticationHistoryRepositoryProvider.get(), singletonCImpl.securityAlertRepositoryProvider.get(), singletonCImpl.appProtectionStateRepositoryProvider.get());

          case 4: // com.aegisauth.feature.security.DeviceSecurityViewModel 
          return (T) new DeviceSecurityViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.aegisauth.feature.enrollment.EnrollmentViewModel 
          return (T) new EnrollmentViewModel(singletonCImpl.imageQualityEngineProvider.get(), singletonCImpl.onDeviceFaceEmbeddingEngineProvider.get(), singletonCImpl.orthonormalBioHashEngineProvider.get(), singletonCImpl.biometricTemplateRepositoryProvider.get(), singletonCImpl.soundFeedbackManagerProvider.get());

          case 6: // com.aegisauth.feature.history.HistoryViewModel 
          return (T) new HistoryViewModel(singletonCImpl.authenticationHistoryRepositoryProvider.get(), singletonCImpl.evidenceManagerProvider.get());

          case 7: // com.aegisauth.feature.onboarding.OnboardingViewModel 
          return (T) new OnboardingViewModel(singletonCImpl.aegisPreferencesProvider.get());

          case 8: // com.aegisauth.feature.protectedapps.ProtectedAppsViewModel 
          return (T) new ProtectedAppsViewModel(singletonCImpl.protectedAppsRepositoryProvider.get());

          case 9: // com.aegisauth.feature.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.aegisPreferencesProvider.get());

          case 10: // com.aegisauth.feature.splash.SplashViewModel 
          return (T) new SplashViewModel(singletonCImpl.aegisPreferencesProvider.get(), singletonCImpl.biometricTemplateRepositoryProvider.get(), singletonCImpl.aegisSessionManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends AegisApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends AegisApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectAppProtectionService(AppProtectionService appProtectionService) {
      injectAppProtectionService2(appProtectionService);
    }

    private AppProtectionService injectAppProtectionService2(AppProtectionService instance) {
      AppProtectionService_MembersInjector.injectProtectedAppsRepository(instance, singletonCImpl.protectedAppsRepositoryProvider.get());
      AppProtectionService_MembersInjector.injectPreferences(instance, singletonCImpl.aegisPreferencesProvider.get());
      AppProtectionService_MembersInjector.injectAppProtectionStateRepository(instance, singletonCImpl.appProtectionStateRepositoryProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends AegisApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AegisDatabase> provideAegisDatabaseProvider;

    private Provider<ProtectedAppsRepository> protectedAppsRepositoryProvider;

    private Provider<PatternSecurityManager> patternSecurityManagerProvider;

    private Provider<PasswordSecurityManager> passwordSecurityManagerProvider;

    private Provider<SecurityAlertRepository> securityAlertRepositoryProvider;

    private Provider<ImageQualityEngine> imageQualityEngineProvider;

    private Provider<PassiveLivenessEngine> passiveLivenessEngineProvider;

    private Provider<RiskEngine> riskEngineProvider;

    private Provider<ActiveChallengeEngine> activeChallengeEngineProvider;

    private Provider<OnDeviceFaceEmbeddingEngine> onDeviceFaceEmbeddingEngineProvider;

    private Provider<KeystoreManager> keystoreManagerProvider;

    private Provider<OrthonormalBioHashEngine> orthonormalBioHashEngineProvider;

    private Provider<BiometricTemplateRepository> biometricTemplateRepositoryProvider;

    private Provider<AuthenticationHistoryRepository> authenticationHistoryRepositoryProvider;

    private Provider<EvidenceManager> evidenceManagerProvider;

    private Provider<SoundFeedbackManager> soundFeedbackManagerProvider;

    private Provider<SystemBiometricManager> systemBiometricManagerProvider;

    private Provider<AegisSessionManager> aegisSessionManagerProvider;

    private Provider<AegisPreferences> aegisPreferencesProvider;

    private Provider<BiometricProfileRepository> biometricProfileRepositoryProvider;

    private Provider<AppProtectionStateRepository> appProtectionStateRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private ProtectedAppDao protectedAppDao() {
      return DatabaseModule_ProvideProtectedAppDaoFactory.provideProtectedAppDao(provideAegisDatabaseProvider.get());
    }

    private SecurityAlertDao securityAlertDao() {
      return DatabaseModule_ProvideSecurityAlertDaoFactory.provideSecurityAlertDao(provideAegisDatabaseProvider.get());
    }

    private BiometricStateDao biometricStateDao() {
      return DatabaseModule_ProvideBiometricStateDaoFactory.provideBiometricStateDao(provideAegisDatabaseProvider.get());
    }

    private AuthenticationEventDao authenticationEventDao() {
      return DatabaseModule_ProvideAuthenticationEventDaoFactory.provideAuthenticationEventDao(provideAegisDatabaseProvider.get());
    }

    private BiometricProfileDao biometricProfileDao() {
      return DatabaseModule_ProvideBiometricProfileDaoFactory.provideBiometricProfileDao(provideAegisDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAegisDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AegisDatabase>(singletonCImpl, 1));
      this.protectedAppsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ProtectedAppsRepository>(singletonCImpl, 0));
      this.patternSecurityManagerProvider = DoubleCheck.provider(new SwitchingProvider<PatternSecurityManager>(singletonCImpl, 2));
      this.passwordSecurityManagerProvider = DoubleCheck.provider(new SwitchingProvider<PasswordSecurityManager>(singletonCImpl, 3));
      this.securityAlertRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SecurityAlertRepository>(singletonCImpl, 4));
      this.imageQualityEngineProvider = DoubleCheck.provider(new SwitchingProvider<ImageQualityEngine>(singletonCImpl, 5));
      this.passiveLivenessEngineProvider = DoubleCheck.provider(new SwitchingProvider<PassiveLivenessEngine>(singletonCImpl, 6));
      this.riskEngineProvider = DoubleCheck.provider(new SwitchingProvider<RiskEngine>(singletonCImpl, 7));
      this.activeChallengeEngineProvider = DoubleCheck.provider(new SwitchingProvider<ActiveChallengeEngine>(singletonCImpl, 8));
      this.onDeviceFaceEmbeddingEngineProvider = DoubleCheck.provider(new SwitchingProvider<OnDeviceFaceEmbeddingEngine>(singletonCImpl, 9));
      this.keystoreManagerProvider = DoubleCheck.provider(new SwitchingProvider<KeystoreManager>(singletonCImpl, 11));
      this.orthonormalBioHashEngineProvider = DoubleCheck.provider(new SwitchingProvider<OrthonormalBioHashEngine>(singletonCImpl, 10));
      this.biometricTemplateRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<BiometricTemplateRepository>(singletonCImpl, 12));
      this.authenticationHistoryRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthenticationHistoryRepository>(singletonCImpl, 13));
      this.evidenceManagerProvider = DoubleCheck.provider(new SwitchingProvider<EvidenceManager>(singletonCImpl, 14));
      this.soundFeedbackManagerProvider = DoubleCheck.provider(new SwitchingProvider<SoundFeedbackManager>(singletonCImpl, 15));
      this.systemBiometricManagerProvider = DoubleCheck.provider(new SwitchingProvider<SystemBiometricManager>(singletonCImpl, 16));
      this.aegisSessionManagerProvider = DoubleCheck.provider(new SwitchingProvider<AegisSessionManager>(singletonCImpl, 17));
      this.aegisPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<AegisPreferences>(singletonCImpl, 18));
      this.biometricProfileRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<BiometricProfileRepository>(singletonCImpl, 19));
      this.appProtectionStateRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AppProtectionStateRepository>(singletonCImpl, 20));
    }

    @Override
    public void injectAegisApp(AegisApp aegisApp) {
    }

    @Override
    public ProtectedAppsRepository protectedAppsRepository() {
      return protectedAppsRepositoryProvider.get();
    }

    @Override
    public PatternSecurityManager patternSecurityManager() {
      return patternSecurityManagerProvider.get();
    }

    @Override
    public PasswordSecurityManager passwordSecurityManager() {
      return passwordSecurityManagerProvider.get();
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.aegisauth.data.repository.ProtectedAppsRepository 
          return (T) new ProtectedAppsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.protectedAppDao());

          case 1: // com.aegisauth.data.local.AegisDatabase 
          return (T) DatabaseModule_ProvideAegisDatabaseFactory.provideAegisDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.aegisauth.core.security.PatternSecurityManager 
          return (T) new PatternSecurityManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.aegisauth.core.security.PasswordSecurityManager 
          return (T) new PasswordSecurityManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.aegisauth.data.repository.SecurityAlertRepository 
          return (T) new SecurityAlertRepository(singletonCImpl.securityAlertDao());

          case 5: // com.aegisauth.domain.quality.ImageQualityEngine 
          return (T) new ImageQualityEngine();

          case 6: // com.aegisauth.domain.liveness.PassiveLivenessEngine 
          return (T) new PassiveLivenessEngine();

          case 7: // com.aegisauth.domain.risk.RiskEngine 
          return (T) new RiskEngine();

          case 8: // com.aegisauth.domain.challenge.ActiveChallengeEngine 
          return (T) new ActiveChallengeEngine();

          case 9: // com.aegisauth.domain.embedding.OnDeviceFaceEmbeddingEngine 
          return (T) new OnDeviceFaceEmbeddingEngine(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // com.aegisauth.domain.biohash.OrthonormalBioHashEngine 
          return (T) new OrthonormalBioHashEngine(singletonCImpl.keystoreManagerProvider.get());

          case 11: // com.aegisauth.core.security.KeystoreManager 
          return (T) new KeystoreManager();

          case 12: // com.aegisauth.data.repository.BiometricTemplateRepository 
          return (T) new BiometricTemplateRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.keystoreManagerProvider.get(), singletonCImpl.orthonormalBioHashEngineProvider.get(), singletonCImpl.biometricStateDao());

          case 13: // com.aegisauth.data.repository.AuthenticationHistoryRepository 
          return (T) new AuthenticationHistoryRepository(singletonCImpl.authenticationEventDao());

          case 14: // com.aegisauth.core.security.EvidenceManager 
          return (T) new EvidenceManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.keystoreManagerProvider.get());

          case 15: // com.aegisauth.core.audio.SoundFeedbackManager 
          return (T) new SoundFeedbackManager();

          case 16: // com.aegisauth.core.biometric.SystemBiometricManager 
          return (T) new SystemBiometricManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 17: // com.aegisauth.core.security.AegisSessionManager 
          return (T) new AegisSessionManager();

          case 18: // com.aegisauth.data.datastore.AegisPreferences 
          return (T) new AegisPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 19: // com.aegisauth.data.repository.BiometricProfileRepository 
          return (T) new BiometricProfileRepository(singletonCImpl.biometricProfileDao(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 20: // com.aegisauth.data.repository.AppProtectionStateRepository 
          return (T) new AppProtectionStateRepository();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
