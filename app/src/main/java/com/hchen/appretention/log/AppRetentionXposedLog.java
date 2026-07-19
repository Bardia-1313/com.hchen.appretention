package com.hchen.appretention.log;
import com.hchen.hooktool.HCData;
import com.hchen.hooktool.HCInit;
import com.hchen.hooktool.log.LogExpand;
import de.robv.android.xposed.XposedBridge;
public final class AppRetentionXposedLog {
    private AppRetentionXposedLog() {}
    public static void logE(String tag, String log) {
        if (HCData.getLogLevel() < HCInit.LOG_E) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log);
    }
    public static void logE(String tag, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_E) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]:\n" + LogExpand.printStackTrace(e));
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]:\n" + LogExpand.printStackTrace(e));
    }
    public static void logE(String tag, String log, String stackTrace) {
        if (HCData.getLogLevel() < HCInit.LOG_E) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log + "\n[Stack Info]: " + stackTrace);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log + "\n[Stack Info]: " + stackTrace);
    }
    public static void logE(String tag, String log, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_E) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
    }
    public static void logENoSave(String tag, String log) {
        if (HCData.getLogLevel() < HCInit.LOG_E) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log);
    }
    public static void logENoSave(String tag, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_E) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]:\n" + LogExpand.printStackTrace(e));
    }
    public static void logENoSave(String tag, String log, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_E) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][E]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
    }
    public static void logW(String tag, String log) {
        if (HCData.getLogLevel() < HCInit.LOG_W) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]: " + log);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]: " + log);
    }
    public static void logW(String tag, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_W) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]:\n" + LogExpand.printStackTrace(e));
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]:\n" + LogExpand.printStackTrace(e));
    }
    public static void logW(String tag, String log, String stackTrace) {
        if (HCData.getLogLevel() < HCInit.LOG_W) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]: " + log + "\n[Stack Info]: " + stackTrace);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]: " + log + "\n[Stack Info]: " + stackTrace);
    }
    public static void logW(String tag, String log, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_W) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][W]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
    }
    public static void logI(String log) {
        if (HCData.getLogLevel() < HCInit.LOG_I) return;
        XposedBridge.log(getXposedTag() + "[I]: " + log);
        SaveLog.saveLogContent("Any", getXposedTag() + "[I]: " + log);
    }
    public static void logI(String tag, String log) {
        if (HCData.getLogLevel() < HCInit.LOG_I) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][I]: " + log);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][I]: " + log);
    }
    public static void logI(String tag, String log, String stackTrace) {
        if (HCData.getLogLevel() < HCInit.LOG_I) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][I]: " + log + "\n[Stack Info]: " + stackTrace);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][I]: " + log + "\n[Stack Info]: " + stackTrace);
    }
    public static void logI(String tag, String log, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_I) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][I]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][I]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
    }
    public static void logINoSave(String tag, String log) {
        if (HCData.getLogLevel() < HCInit.LOG_I) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][I]: " + log);
    }
    public static void logD(String tag, String log) {
        if (HCData.getLogLevel() < HCInit.LOG_D) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]: " + log);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]: " + log);
    }
    public static void logD(String tag, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_D) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]:\n" + LogExpand.printStackTrace(e));
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]:\n" + LogExpand.printStackTrace(e));
    }
    public static void logD(String tag, String log, String stackTrace) {
        if (HCData.getLogLevel() < HCInit.LOG_D) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]: " + log + "\n[Stack Info]: " + stackTrace);
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]: " + log + "\n[Stack Info]: " + stackTrace);
    }
    public static void logD(String tag, String log, Throwable e) {
        if (HCData.getLogLevel() < HCInit.LOG_D) return;
        XposedBridge.log(getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
        SaveLog.saveLogContent(tag, getXposedTag() + "[" + HCData.getTargetPackageName() + "][" + tag + "][D]: " + log + "\n[Stack Info]: " + LogExpand.printStackTrace(e));
    }
    private static String getXposedTag() {
        return "[" + HCData.getTag() + "]";
    }
}
