package com.aegisauth.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aegisauth.data.local.entity.AuthenticationEvent;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AuthenticationEventDao_Impl implements AuthenticationEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AuthenticationEvent> __insertionAdapterOfAuthenticationEvent;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public AuthenticationEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAuthenticationEvent = new EntityInsertionAdapter<AuthenticationEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `authentication_events` (`id`,`timestamp`,`packageName`,`appName`,`authMethod`,`result`,`riskLevel`,`livenessScore`,`qualityScore`,`similarityScore`,`challengeRequired`,`challengeResult`,`failureReason`,`evidencePath`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AuthenticationEvent entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getPackageName());
        statement.bindString(4, entity.getAppName());
        statement.bindString(5, entity.getAuthMethod());
        statement.bindString(6, entity.getResult());
        statement.bindString(7, entity.getRiskLevel());
        statement.bindDouble(8, entity.getLivenessScore());
        statement.bindDouble(9, entity.getQualityScore());
        statement.bindDouble(10, entity.getSimilarityScore());
        final int _tmp = entity.getChallengeRequired() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getChallengeResult() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getChallengeResult());
        }
        if (entity.getFailureReason() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getFailureReason());
        }
        if (entity.getEvidencePath() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEvidencePath());
        }
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM authentication_events";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AuthenticationEvent event,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAuthenticationEvent.insertAndReturnId(event);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AuthenticationEvent>> getAllEvents() {
    final String _sql = "SELECT * FROM authentication_events ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"authentication_events"}, new Callable<List<AuthenticationEvent>>() {
      @Override
      @NonNull
      public List<AuthenticationEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAuthMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "authMethod");
          final int _cursorIndexOfResult = CursorUtil.getColumnIndexOrThrow(_cursor, "result");
          final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "riskLevel");
          final int _cursorIndexOfLivenessScore = CursorUtil.getColumnIndexOrThrow(_cursor, "livenessScore");
          final int _cursorIndexOfQualityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "qualityScore");
          final int _cursorIndexOfSimilarityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "similarityScore");
          final int _cursorIndexOfChallengeRequired = CursorUtil.getColumnIndexOrThrow(_cursor, "challengeRequired");
          final int _cursorIndexOfChallengeResult = CursorUtil.getColumnIndexOrThrow(_cursor, "challengeResult");
          final int _cursorIndexOfFailureReason = CursorUtil.getColumnIndexOrThrow(_cursor, "failureReason");
          final int _cursorIndexOfEvidencePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidencePath");
          final List<AuthenticationEvent> _result = new ArrayList<AuthenticationEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuthenticationEvent _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpAuthMethod;
            _tmpAuthMethod = _cursor.getString(_cursorIndexOfAuthMethod);
            final String _tmpResult;
            _tmpResult = _cursor.getString(_cursorIndexOfResult);
            final String _tmpRiskLevel;
            _tmpRiskLevel = _cursor.getString(_cursorIndexOfRiskLevel);
            final float _tmpLivenessScore;
            _tmpLivenessScore = _cursor.getFloat(_cursorIndexOfLivenessScore);
            final float _tmpQualityScore;
            _tmpQualityScore = _cursor.getFloat(_cursorIndexOfQualityScore);
            final float _tmpSimilarityScore;
            _tmpSimilarityScore = _cursor.getFloat(_cursorIndexOfSimilarityScore);
            final boolean _tmpChallengeRequired;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfChallengeRequired);
            _tmpChallengeRequired = _tmp != 0;
            final String _tmpChallengeResult;
            if (_cursor.isNull(_cursorIndexOfChallengeResult)) {
              _tmpChallengeResult = null;
            } else {
              _tmpChallengeResult = _cursor.getString(_cursorIndexOfChallengeResult);
            }
            final String _tmpFailureReason;
            if (_cursor.isNull(_cursorIndexOfFailureReason)) {
              _tmpFailureReason = null;
            } else {
              _tmpFailureReason = _cursor.getString(_cursorIndexOfFailureReason);
            }
            final String _tmpEvidencePath;
            if (_cursor.isNull(_cursorIndexOfEvidencePath)) {
              _tmpEvidencePath = null;
            } else {
              _tmpEvidencePath = _cursor.getString(_cursorIndexOfEvidencePath);
            }
            _item = new AuthenticationEvent(_tmpId,_tmpTimestamp,_tmpPackageName,_tmpAppName,_tmpAuthMethod,_tmpResult,_tmpRiskLevel,_tmpLivenessScore,_tmpQualityScore,_tmpSimilarityScore,_tmpChallengeRequired,_tmpChallengeResult,_tmpFailureReason,_tmpEvidencePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<AuthenticationEvent>> getRecentEvents(final int limit) {
    final String _sql = "SELECT * FROM authentication_events ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"authentication_events"}, new Callable<List<AuthenticationEvent>>() {
      @Override
      @NonNull
      public List<AuthenticationEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAuthMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "authMethod");
          final int _cursorIndexOfResult = CursorUtil.getColumnIndexOrThrow(_cursor, "result");
          final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "riskLevel");
          final int _cursorIndexOfLivenessScore = CursorUtil.getColumnIndexOrThrow(_cursor, "livenessScore");
          final int _cursorIndexOfQualityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "qualityScore");
          final int _cursorIndexOfSimilarityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "similarityScore");
          final int _cursorIndexOfChallengeRequired = CursorUtil.getColumnIndexOrThrow(_cursor, "challengeRequired");
          final int _cursorIndexOfChallengeResult = CursorUtil.getColumnIndexOrThrow(_cursor, "challengeResult");
          final int _cursorIndexOfFailureReason = CursorUtil.getColumnIndexOrThrow(_cursor, "failureReason");
          final int _cursorIndexOfEvidencePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidencePath");
          final List<AuthenticationEvent> _result = new ArrayList<AuthenticationEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuthenticationEvent _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpAuthMethod;
            _tmpAuthMethod = _cursor.getString(_cursorIndexOfAuthMethod);
            final String _tmpResult;
            _tmpResult = _cursor.getString(_cursorIndexOfResult);
            final String _tmpRiskLevel;
            _tmpRiskLevel = _cursor.getString(_cursorIndexOfRiskLevel);
            final float _tmpLivenessScore;
            _tmpLivenessScore = _cursor.getFloat(_cursorIndexOfLivenessScore);
            final float _tmpQualityScore;
            _tmpQualityScore = _cursor.getFloat(_cursorIndexOfQualityScore);
            final float _tmpSimilarityScore;
            _tmpSimilarityScore = _cursor.getFloat(_cursorIndexOfSimilarityScore);
            final boolean _tmpChallengeRequired;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfChallengeRequired);
            _tmpChallengeRequired = _tmp != 0;
            final String _tmpChallengeResult;
            if (_cursor.isNull(_cursorIndexOfChallengeResult)) {
              _tmpChallengeResult = null;
            } else {
              _tmpChallengeResult = _cursor.getString(_cursorIndexOfChallengeResult);
            }
            final String _tmpFailureReason;
            if (_cursor.isNull(_cursorIndexOfFailureReason)) {
              _tmpFailureReason = null;
            } else {
              _tmpFailureReason = _cursor.getString(_cursorIndexOfFailureReason);
            }
            final String _tmpEvidencePath;
            if (_cursor.isNull(_cursorIndexOfEvidencePath)) {
              _tmpEvidencePath = null;
            } else {
              _tmpEvidencePath = _cursor.getString(_cursorIndexOfEvidencePath);
            }
            _item = new AuthenticationEvent(_tmpId,_tmpTimestamp,_tmpPackageName,_tmpAppName,_tmpAuthMethod,_tmpResult,_tmpRiskLevel,_tmpLivenessScore,_tmpQualityScore,_tmpSimilarityScore,_tmpChallengeRequired,_tmpChallengeResult,_tmpFailureReason,_tmpEvidencePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getEventsSince(final long sinceTimestamp,
      final Continuation<? super List<AuthenticationEvent>> $completion) {
    final String _sql = "SELECT * FROM authentication_events WHERE timestamp >= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AuthenticationEvent>>() {
      @Override
      @NonNull
      public List<AuthenticationEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfAuthMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "authMethod");
          final int _cursorIndexOfResult = CursorUtil.getColumnIndexOrThrow(_cursor, "result");
          final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "riskLevel");
          final int _cursorIndexOfLivenessScore = CursorUtil.getColumnIndexOrThrow(_cursor, "livenessScore");
          final int _cursorIndexOfQualityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "qualityScore");
          final int _cursorIndexOfSimilarityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "similarityScore");
          final int _cursorIndexOfChallengeRequired = CursorUtil.getColumnIndexOrThrow(_cursor, "challengeRequired");
          final int _cursorIndexOfChallengeResult = CursorUtil.getColumnIndexOrThrow(_cursor, "challengeResult");
          final int _cursorIndexOfFailureReason = CursorUtil.getColumnIndexOrThrow(_cursor, "failureReason");
          final int _cursorIndexOfEvidencePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidencePath");
          final List<AuthenticationEvent> _result = new ArrayList<AuthenticationEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuthenticationEvent _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpAuthMethod;
            _tmpAuthMethod = _cursor.getString(_cursorIndexOfAuthMethod);
            final String _tmpResult;
            _tmpResult = _cursor.getString(_cursorIndexOfResult);
            final String _tmpRiskLevel;
            _tmpRiskLevel = _cursor.getString(_cursorIndexOfRiskLevel);
            final float _tmpLivenessScore;
            _tmpLivenessScore = _cursor.getFloat(_cursorIndexOfLivenessScore);
            final float _tmpQualityScore;
            _tmpQualityScore = _cursor.getFloat(_cursorIndexOfQualityScore);
            final float _tmpSimilarityScore;
            _tmpSimilarityScore = _cursor.getFloat(_cursorIndexOfSimilarityScore);
            final boolean _tmpChallengeRequired;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfChallengeRequired);
            _tmpChallengeRequired = _tmp != 0;
            final String _tmpChallengeResult;
            if (_cursor.isNull(_cursorIndexOfChallengeResult)) {
              _tmpChallengeResult = null;
            } else {
              _tmpChallengeResult = _cursor.getString(_cursorIndexOfChallengeResult);
            }
            final String _tmpFailureReason;
            if (_cursor.isNull(_cursorIndexOfFailureReason)) {
              _tmpFailureReason = null;
            } else {
              _tmpFailureReason = _cursor.getString(_cursorIndexOfFailureReason);
            }
            final String _tmpEvidencePath;
            if (_cursor.isNull(_cursorIndexOfEvidencePath)) {
              _tmpEvidencePath = null;
            } else {
              _tmpEvidencePath = _cursor.getString(_cursorIndexOfEvidencePath);
            }
            _item = new AuthenticationEvent(_tmpId,_tmpTimestamp,_tmpPackageName,_tmpAppName,_tmpAuthMethod,_tmpResult,_tmpRiskLevel,_tmpLivenessScore,_tmpQualityScore,_tmpSimilarityScore,_tmpChallengeRequired,_tmpChallengeResult,_tmpFailureReason,_tmpEvidencePath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentFailedAttemptsCount(final long sinceTimestamp,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM authentication_events WHERE result = 'FAILURE' AND timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentFailedAttemptsCountForPackage(final String packageName,
      final long sinceTimestamp, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM authentication_events WHERE result = 'FAILURE' AND packageName = ? AND timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, packageName);
    _argIndex = 2;
    _statement.bindLong(_argIndex, sinceTimestamp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
