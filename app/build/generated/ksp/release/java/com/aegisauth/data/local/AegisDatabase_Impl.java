package com.aegisauth.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.aegisauth.data.local.dao.AuthenticationEventDao;
import com.aegisauth.data.local.dao.AuthenticationEventDao_Impl;
import com.aegisauth.data.local.dao.BiometricProfileDao;
import com.aegisauth.data.local.dao.BiometricProfileDao_Impl;
import com.aegisauth.data.local.dao.BiometricStateDao;
import com.aegisauth.data.local.dao.BiometricStateDao_Impl;
import com.aegisauth.data.local.dao.ProtectedAppDao;
import com.aegisauth.data.local.dao.ProtectedAppDao_Impl;
import com.aegisauth.data.local.dao.SecurityAlertDao;
import com.aegisauth.data.local.dao.SecurityAlertDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AegisDatabase_Impl extends AegisDatabase {
  private volatile ProtectedAppDao _protectedAppDao;

  private volatile AuthenticationEventDao _authenticationEventDao;

  private volatile SecurityAlertDao _securityAlertDao;

  private volatile BiometricStateDao _biometricStateDao;

  private volatile BiometricProfileDao _biometricProfileDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `protected_applications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `isProtected` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_protected_applications_packageName` ON `protected_applications` (`packageName`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `authentication_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `authMethod` TEXT NOT NULL, `result` TEXT NOT NULL, `riskLevel` TEXT NOT NULL, `livenessScore` REAL NOT NULL, `qualityScore` REAL NOT NULL, `similarityScore` REAL NOT NULL, `challengeRequired` INTEGER NOT NULL, `challengeResult` TEXT, `failureReason` TEXT, `evidencePath` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `security_alerts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `severity` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `isRead` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `biometric_state` (`id` INTEGER NOT NULL, `enrollmentVersion` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `samplesCount` INTEGER NOT NULL, `templateHash` TEXT NOT NULL, `profileId` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `biometric_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `displayName` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6929b9dbb74525d8308bd62e31ae0fd0')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `protected_applications`");
        db.execSQL("DROP TABLE IF EXISTS `authentication_events`");
        db.execSQL("DROP TABLE IF EXISTS `security_alerts`");
        db.execSQL("DROP TABLE IF EXISTS `biometric_state`");
        db.execSQL("DROP TABLE IF EXISTS `biometric_profiles`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsProtectedApplications = new HashMap<String, TableInfo.Column>(6);
        _columnsProtectedApplications.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProtectedApplications.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProtectedApplications.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProtectedApplications.put("isProtected", new TableInfo.Column("isProtected", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProtectedApplications.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProtectedApplications.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProtectedApplications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProtectedApplications = new HashSet<TableInfo.Index>(1);
        _indicesProtectedApplications.add(new TableInfo.Index("index_protected_applications_packageName", true, Arrays.asList("packageName"), Arrays.asList("ASC")));
        final TableInfo _infoProtectedApplications = new TableInfo("protected_applications", _columnsProtectedApplications, _foreignKeysProtectedApplications, _indicesProtectedApplications);
        final TableInfo _existingProtectedApplications = TableInfo.read(db, "protected_applications");
        if (!_infoProtectedApplications.equals(_existingProtectedApplications)) {
          return new RoomOpenHelper.ValidationResult(false, "protected_applications(com.aegisauth.data.local.entity.ProtectedApplication).\n"
                  + " Expected:\n" + _infoProtectedApplications + "\n"
                  + " Found:\n" + _existingProtectedApplications);
        }
        final HashMap<String, TableInfo.Column> _columnsAuthenticationEvents = new HashMap<String, TableInfo.Column>(14);
        _columnsAuthenticationEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("authMethod", new TableInfo.Column("authMethod", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("result", new TableInfo.Column("result", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("riskLevel", new TableInfo.Column("riskLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("livenessScore", new TableInfo.Column("livenessScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("qualityScore", new TableInfo.Column("qualityScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("similarityScore", new TableInfo.Column("similarityScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("challengeRequired", new TableInfo.Column("challengeRequired", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("challengeResult", new TableInfo.Column("challengeResult", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("failureReason", new TableInfo.Column("failureReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuthenticationEvents.put("evidencePath", new TableInfo.Column("evidencePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAuthenticationEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAuthenticationEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAuthenticationEvents = new TableInfo("authentication_events", _columnsAuthenticationEvents, _foreignKeysAuthenticationEvents, _indicesAuthenticationEvents);
        final TableInfo _existingAuthenticationEvents = TableInfo.read(db, "authentication_events");
        if (!_infoAuthenticationEvents.equals(_existingAuthenticationEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "authentication_events(com.aegisauth.data.local.entity.AuthenticationEvent).\n"
                  + " Expected:\n" + _infoAuthenticationEvents + "\n"
                  + " Found:\n" + _existingAuthenticationEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsSecurityAlerts = new HashMap<String, TableInfo.Column>(7);
        _columnsSecurityAlerts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityAlerts.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityAlerts.put("severity", new TableInfo.Column("severity", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityAlerts.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityAlerts.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityAlerts.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSecurityAlerts.put("isRead", new TableInfo.Column("isRead", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSecurityAlerts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSecurityAlerts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSecurityAlerts = new TableInfo("security_alerts", _columnsSecurityAlerts, _foreignKeysSecurityAlerts, _indicesSecurityAlerts);
        final TableInfo _existingSecurityAlerts = TableInfo.read(db, "security_alerts");
        if (!_infoSecurityAlerts.equals(_existingSecurityAlerts)) {
          return new RoomOpenHelper.ValidationResult(false, "security_alerts(com.aegisauth.data.local.entity.SecurityAlert).\n"
                  + " Expected:\n" + _infoSecurityAlerts + "\n"
                  + " Found:\n" + _existingSecurityAlerts);
        }
        final HashMap<String, TableInfo.Column> _columnsBiometricState = new HashMap<String, TableInfo.Column>(8);
        _columnsBiometricState.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricState.put("enrollmentVersion", new TableInfo.Column("enrollmentVersion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricState.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricState.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricState.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricState.put("samplesCount", new TableInfo.Column("samplesCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricState.put("templateHash", new TableInfo.Column("templateHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricState.put("profileId", new TableInfo.Column("profileId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBiometricState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBiometricState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBiometricState = new TableInfo("biometric_state", _columnsBiometricState, _foreignKeysBiometricState, _indicesBiometricState);
        final TableInfo _existingBiometricState = TableInfo.read(db, "biometric_state");
        if (!_infoBiometricState.equals(_existingBiometricState)) {
          return new RoomOpenHelper.ValidationResult(false, "biometric_state(com.aegisauth.data.local.entity.BiometricState).\n"
                  + " Expected:\n" + _infoBiometricState + "\n"
                  + " Found:\n" + _existingBiometricState);
        }
        final HashMap<String, TableInfo.Column> _columnsBiometricProfiles = new HashMap<String, TableInfo.Column>(3);
        _columnsBiometricProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricProfiles.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBiometricProfiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBiometricProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBiometricProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBiometricProfiles = new TableInfo("biometric_profiles", _columnsBiometricProfiles, _foreignKeysBiometricProfiles, _indicesBiometricProfiles);
        final TableInfo _existingBiometricProfiles = TableInfo.read(db, "biometric_profiles");
        if (!_infoBiometricProfiles.equals(_existingBiometricProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "biometric_profiles(com.aegisauth.data.local.entity.BiometricProfile).\n"
                  + " Expected:\n" + _infoBiometricProfiles + "\n"
                  + " Found:\n" + _existingBiometricProfiles);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6929b9dbb74525d8308bd62e31ae0fd0", "34492535a4f288fce7940e7716593747");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "protected_applications","authentication_events","security_alerts","biometric_state","biometric_profiles");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `protected_applications`");
      _db.execSQL("DELETE FROM `authentication_events`");
      _db.execSQL("DELETE FROM `security_alerts`");
      _db.execSQL("DELETE FROM `biometric_state`");
      _db.execSQL("DELETE FROM `biometric_profiles`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProtectedAppDao.class, ProtectedAppDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AuthenticationEventDao.class, AuthenticationEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SecurityAlertDao.class, SecurityAlertDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BiometricStateDao.class, BiometricStateDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BiometricProfileDao.class, BiometricProfileDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ProtectedAppDao protectedAppDao() {
    if (_protectedAppDao != null) {
      return _protectedAppDao;
    } else {
      synchronized(this) {
        if(_protectedAppDao == null) {
          _protectedAppDao = new ProtectedAppDao_Impl(this);
        }
        return _protectedAppDao;
      }
    }
  }

  @Override
  public AuthenticationEventDao authenticationEventDao() {
    if (_authenticationEventDao != null) {
      return _authenticationEventDao;
    } else {
      synchronized(this) {
        if(_authenticationEventDao == null) {
          _authenticationEventDao = new AuthenticationEventDao_Impl(this);
        }
        return _authenticationEventDao;
      }
    }
  }

  @Override
  public SecurityAlertDao securityAlertDao() {
    if (_securityAlertDao != null) {
      return _securityAlertDao;
    } else {
      synchronized(this) {
        if(_securityAlertDao == null) {
          _securityAlertDao = new SecurityAlertDao_Impl(this);
        }
        return _securityAlertDao;
      }
    }
  }

  @Override
  public BiometricStateDao biometricStateDao() {
    if (_biometricStateDao != null) {
      return _biometricStateDao;
    } else {
      synchronized(this) {
        if(_biometricStateDao == null) {
          _biometricStateDao = new BiometricStateDao_Impl(this);
        }
        return _biometricStateDao;
      }
    }
  }

  @Override
  public BiometricProfileDao biometricProfileDao() {
    if (_biometricProfileDao != null) {
      return _biometricProfileDao;
    } else {
      synchronized(this) {
        if(_biometricProfileDao == null) {
          _biometricProfileDao = new BiometricProfileDao_Impl(this);
        }
        return _biometricProfileDao;
      }
    }
  }
}
