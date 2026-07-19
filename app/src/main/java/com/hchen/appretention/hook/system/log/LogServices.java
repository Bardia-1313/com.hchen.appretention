package com.hchen.appretention.hook.system.log;
import static com.hchen.appretention.data.method.SystemMethod.systemReady;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.TimingsTraceAndSlog;
import static com.hchen.appretention.data.prop.SystemProp.FALSE;
import static com.hchen.appretention.data.prop.SystemProp.TRUE;
import static com.hchen.appretention.hook.system.log.LogServices.KillEventLogRecord.SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import com.hchen.appretention.BuildConfig;
import com.hchen.appretention.log.SaveLog;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.log.XposedLog;
import com.hchen.appretention.log.AppRetentionXposedLog;
import com.hchen.hooktool.utils.SystemPropTool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@HookEntrance(targetPackage = "android")
public class LogServices extends HCBase {
    private Context mContext;
    public static boolean mSupportLogServices = true;
    @Override
    protected void init() {
        Method systemReadyMethod;
        if (existsMethod(ActivityManagerService, systemReady, Runnable.class, TimingsTraceAndSlog))
            systemReadyMethod = findMethod(ActivityManagerService, systemReady, Runnable.class, TimingsTraceAndSlog);
        else {
            mSupportLogServices = false;
            logW(TAG, "Your Device Not Support LogServices!!");
            return;
        }
        SystemPropTool.setProp(SaveLog.USER_UNLOCKED_COMPLETED_PROP, FALSE);
        hook(systemReadyMethod,
            new IHook() {
                @Override
                @SuppressLint("UnspecifiedRegisterReceiverFlag")
                public void after() {
                    mContext = (Context) getThisField("mContext");
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_BOOT_COMPLETED);
                    filter.addAction(Intent.ACTION_SHUTDOWN);
                    filter.addAction(Intent.ACTION_REBOOT);
                    filter.addAction(SaveLog.ACTION_LOG_SERVICE_CONTENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mContext.registerReceiver(new SystemBaseBroadcastReceiver(), filter, Context.RECEIVER_EXPORTED);
                    } else
                        mContext.registerReceiver(new SystemBaseBroadcastReceiver(), filter);
                    XposedLog.logI(TAG, "Register log services broadcast receiver!!");
                }
            }
        );
    }
    private static class SystemBaseBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "LogServices";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Intent.ACTION_BOOT_COMPLETED -> {
                        SystemPropTool.setProp(SaveLog.USER_UNLOCKED_COMPLETED_PROP, TRUE);
                        KillEventLogRecord.init(context);
                        RecordSystemProp.startRecord();
                        context.getContentResolver().registerContentObserver(Settings.System.getUriFor(SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE),
                            false, new ContentObserver(new Handler(context.getMainLooper())) {
                                @Override
                                public void onChange(boolean selfChange) {
                                    if (selfChange) return;
                                    KillEventLogRecord.init(context);
                                }
                            }
                        );
                        XposedLog.logI(TAG, "System boot completed!!");
                    }
                    case SaveLog.ACTION_LOG_SERVICE_CONTENT -> {
                        SaveLog.LogContentData logContentData;
                        try {
                            logContentData = getLogContentData(intent);
                            if (logContentData == null) {
                                XposedLog.logW(TAG, "Broadcast receiver: log logContent data is null!");
                                return;
                            }
                        } catch (Throwable ignore) {
                            return;
                        }
                        String logId = logContentData.mLogId;
                        String fileName = logContentData.mLogFileName;
                        ArrayList<String> logContent = logContentData.mLogContentCache;
                        SaveLog.openFile(fileName, logId);
                        SaveLog.writeFile(fileName, logContent);
                        setResultCode(Activity.RESULT_OK);
                        AndroidLog.logI(TAG, "Broadcast receiver: fileName: " + fileName + ", logId: " + logId + ", logContent: " + logContent);
                    }
                    case Intent.ACTION_SHUTDOWN, Intent.ACTION_REBOOT -> {
                        SaveLog.removeAllOldLogFileAndCopyLogFileToOldPathIfNeed();
                        AppRetentionXposedLog.logINoSave(TAG, "System will shutdown or reboot!!!");
                    }
                    default -> {
                        XposedLog.logW(TAG, "Unknown action: " + intent.getAction());
                    }
                }
            }
        }
        private static SaveLog.LogContentData getLogContentData(Intent intent) throws ReflectiveOperationException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return intent.getParcelableExtra("logData", SaveLog.LogContentData.class);
            }
            Object rawData = getLegacyParcelableExtra(intent, "logData");
            return rawData instanceof SaveLog.LogContentData ? (SaveLog.LogContentData) rawData : null;
        }
        private static Object getLegacyParcelableExtra(Intent intent, String key) throws ReflectiveOperationException {
            Method getParcelableExtra = Intent.class.getMethod("getParcelableExtra", String.class);
            return getParcelableExtra.invoke(intent, key);
        }
    }
    static class KillEventLogRecord {
        public static final String SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE = "kill_event_log_record_enable";
        private static final String TAG = "KillEventLogRecord";
        private static final String mKillEventRecordFile = "KillEvent";
        private static boolean isKillEventRecording = false;
        private static ExecutorService mExecutorService;
        private static BufferedReader mReader;
        private static Process mLogcat;
        private static void init(Context context) {
            if (BuildConfig.DEBUG || TRUE.equals(Settings.System.getString(context.getContentResolver(), SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE))) {
                if (!isKillEventRecording)
                    startRecord();
                else
                    XposedLog.logW(TAG, "Kill event log record is already started!!");
            } else if (!BuildConfig.DEBUG) {
                if (isKillEventRecording && mExecutorService != null) {
                    mExecutorService.shutdownNow();
                    clear();
                    XposedLog.logI(TAG, "Stop record kill event!!");
                }
            }
        }
        private static void startRecord() {
            SaveLog.openFile(mKillEventRecordFile, SaveLog.getRandomNumber());
            mExecutorService = Executors.newSingleThreadExecutor();
            mExecutorService.submit(() -> {
                try {
                    XposedLog.logI(TAG, "Start record kill event!!");
                    isKillEventRecording = true;
                    mLogcat = Runtime.getRuntime().exec("logcat -b events");
                    mReader = new BufferedReader(new InputStreamReader(mLogcat.getInputStream()));
                    String line;
                    while ((line = mReader.readLine()) != null) {
                        if (line.isEmpty())
                            continue;
                        String lowerCaseLine = line.toLowerCase();
                        if (lowerCaseLine.contains("kill") && !lowerCaseLine.contains("killinfo"))
                            SaveLog.writeFile(mKillEventRecordFile, line);
                    }
                } catch (IOException e) {
                    XposedLog.logE(TAG, "Start record kill event failed!", e);
                } finally {
                    if (mLogcat != null) {
                        mLogcat.destroy();
                        mLogcat = null;
                    }
                    if (mReader != null) {
                        try {
                            mReader.close();
                            mReader = null;
                        } catch (IOException e) {
                            XposedLog.logE(TAG, "Close reader failed!", e);
                        }
                    }
                    SaveLog.closeFile(mKillEventRecordFile);
                    isKillEventRecording = false;
                }
            });
        }
        private static void clear() {
            SaveLog.closeFile(mKillEventRecordFile);
            if (mLogcat != null) {
                mLogcat.destroy();
                mLogcat = null;
            }
            if (mReader != null) {
                try {
                    mReader.close();
                    mReader = null;
                } catch (IOException e) {
                    XposedLog.logE(TAG, "Close reader failed!", e);
                }
            }
            isKillEventRecording = false;
            XposedLog.logI(TAG, "Clear kll log event record process!!");
        }
    }
    static class RecordSystemProp {
        private static final String mRecordFile = "SystemProp";
        private static final String TAG = "RecordSystemProp";
        private static Process mPropData;
        private static BufferedReader mReader;
        public static void startRecord() {
            SaveLog.openFile(mRecordFile, SaveLog.getRandomNumber());
            ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
            mExecutorService.submit(() -> {
                try {
                    XposedLog.logI(TAG, "Start record system prop!!");
                    mPropData = Runtime.getRuntime().exec("getprop");
                    mReader = new BufferedReader(new InputStreamReader(mPropData.getInputStream()));
                    String line;
                    while ((line = mReader.readLine()) != null) {
                        if (line.isEmpty())
                            continue;
                        SaveLog.writeFile(mRecordFile, line);
                    }
                } catch (IOException e) {
                    XposedLog.logE(TAG, "Start record system prop failed!", e);
                } finally {
                    if (mPropData != null) {
                        mPropData.destroy();
                        mPropData = null;
                    }
                    if (mReader != null) {
                        try {
                            mReader.close();
                            mReader = null;
                        } catch (IOException e) {
                            XposedLog.logE(TAG, "Close reader failed!", e);
                        }
                    }
                    SaveLog.closeFile(mRecordFile);
                    XposedLog.logI(TAG, "Record system prop done, close process success!!");
                }
            });
        }
    }
}
