package com.hchen.appretention.hook.system.opt;
import static com.hchen.appretention.data.method.SystemMethod.cleanUpRemovedTask;
import static com.hchen.appretention.data.method.SystemMethod.forceFreezeAppAsyncLSP;
import static com.hchen.appretention.data.method.SystemMethod.freezeAppAsyncAtEarliestLSP;
import static com.hchen.appretention.data.method.SystemMethod.freezeAppAsyncImmediateLSP;
import static com.hchen.appretention.data.method.SystemMethod.freezeAppAsyncInternalLSP;
import static com.hchen.appretention.data.method.SystemMethod.freezeAppAsyncLSP;
import static com.hchen.appretention.data.method.SystemMethod.killAllBackgroundProcesses;
import static com.hchen.appretention.data.method.SystemMethod.killAllBackgroundProcessesExcept;
import static com.hchen.appretention.data.method.SystemMethod.killAllBackgroundProcessesExceptLSP;
import static com.hchen.appretention.data.method.SystemMethod.killBackgroundProcesses;
import static com.hchen.appretention.data.method.SystemMethod.killPackageProcesses;
import static com.hchen.appretention.data.method.SystemMethod.killPackageProcessesLocked;
import static com.hchen.appretention.data.method.SystemMethod.killPackageProcessesLSP;
import static com.hchen.appretention.data.method.SystemMethod.killProcessesForRemovedTask;
import static com.hchen.appretention.data.method.SystemMethod.killTaskProcessesIfPossible;
import static com.hchen.appretention.data.method.SystemMethod.removeTask;
import static com.hchen.appretention.data.method.SystemMethod.removeTaskById;
import static com.hchen.appretention.data.method.SystemMethod.stopAppForUser;
import static com.hchen.appretention.data.method.SystemMethod.stopAppForUserInternal;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService$LocalService;
import static com.hchen.appretention.data.path.SystemClass.ActivityTaskSupervisor;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.appretention.log.AppRetentionXposedLog.logD;
import static com.hchen.appretention.log.AppRetentionXposedLog.logW;
import static com.hchen.hooktool.core.CoreTool.findAllMethod;
import static com.hchen.hooktool.core.CoreTool.findClass;
import static com.hchen.hooktool.core.CoreTool.hook;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.utils.SystemPropTool;
import java.lang.reflect.Method;
import java.util.Locale;
public final class ForceStopOnlyPolicy {
    private static final String TAG = "ForceStopOnlyPolicy";
    private static final int EXIT_REASON_USER_REQUESTED = 10;
    private static final int EXIT_REASON_PERMISSION_CHANGE = 8;
    private static final int EXIT_REASON_USER_STOPPED = 11;
    private static final int EXIT_REASON_PACKAGE_STATE_CHANGE = 15;
    private static final int EXIT_REASON_PACKAGE_UPDATED = 16;
    private static final int EXIT_REASON_PACKAGE_REMOVED = 13;
    private static final int EXIT_SUBREASON_FORCE_STOP = 21;
    private static final int EXIT_SUBREASON_PACKAGE_UPDATE = 25;
    private static final String PROP_ENABLE = "persist.hchen.retention.force_stop_only";
    private static final String PROP_DISABLE_FREEZER = "persist.hchen.retention.disable_freezer";
    private static final String PROP_PROTECT_RECENTS = "persist.hchen.retention.protect_recents";
    private ForceStopOnlyPolicy() {
    }
    public static void init() {
        if (!SystemPropTool.getProp(PROP_ENABLE, true)) {
            logD(TAG, "Force-stop-only retention is disabled.");
            return;
        }
        hookActivityManagerKillEntrypoints();
        hookProcessListKillEntrypoints();
        hookRecentTaskRemoval();
        hookCachedAppFreezer();
    }
    private static void hookActivityManagerKillEntrypoints() {
        hookAllIfExists(ActivityManagerService, killBackgroundProcesses, blockCurrentMethodHook());
        hookAllIfExists(ActivityManagerService, killAllBackgroundProcesses, blockCurrentMethodHook());
        hookAllIfExists(ActivityManagerService, killAllBackgroundProcessesExcept, blockCurrentMethodHook());
        hookAllIfExists(ActivityManagerService, stopAppForUser, blockCurrentMethodHook());
        hookAllIfExists(ActivityManagerService, stopAppForUserInternal, blockCurrentMethodHook());
    }
    private static void hookProcessListKillEntrypoints() {
        hookAllIfExists(ProcessList,
            killPackageProcessesLSP,
            new MethodHookFactory() {
                @Override
                public IHook create(final Method method) {
                    return new IHook() {
                        @Override
                        public void before() {
                            if (isAllowedPackageKill(this, method)) return;
                            setResult(defaultValue(method.getReturnType()));
                        }
                    };
                }
            }
        );
        hookAllIfExists(ProcessList,
            killPackageProcessesLocked,
            packageKillGateFactory()
        );
        hookAllIfExists(ProcessList,
            killPackageProcesses,
            packageKillGateFactory()
        );
        hookAllIfExists(ProcessList,
            killAllBackgroundProcessesExceptLSP,
            blockCurrentMethodHook()
        );
    }
    private static void hookRecentTaskRemoval() {
        if (!SystemPropTool.getProp(PROP_PROTECT_RECENTS, true)) {
            logD(TAG, "Recent-task protection is disabled.");
            return;
        }
        hookAllIfExists(ActivityTaskSupervisor, removeTaskById, forceNoKillProcessFlagHook());
        hookAllIfExists(ActivityTaskSupervisor, removeTask, forceNoKillProcessFlagHook());
        hookAllIfExists(ActivityTaskSupervisor, cleanUpRemovedTask, forceNoKillProcessFlagHook());
        hookAllIfExists(ActivityTaskSupervisor, killTaskProcessesIfPossible, blockCurrentMethodHook());
        hookAllIfExists(ActivityManagerService$LocalService,
            killProcessesForRemovedTask, blockCurrentMethodHook());
    }
    private static void hookCachedAppFreezer() {
        if (!SystemPropTool.getProp(PROP_DISABLE_FREEZER, true)) {
            logD(TAG, "Cached-app freezer protection is disabled.");
            return;
        }
        hookAllIfExists(CachedAppOptimizer, freezeAppAsyncLSP, blockCurrentMethodHook());
        hookAllIfExists(CachedAppOptimizer, forceFreezeAppAsyncLSP, blockCurrentMethodHook());
        hookAllIfExists(CachedAppOptimizer, freezeAppAsyncAtEarliestLSP, blockCurrentMethodHook());
        hookAllIfExists(CachedAppOptimizer, freezeAppAsyncImmediateLSP, blockCurrentMethodHook());
        hookAllIfExists(CachedAppOptimizer, freezeAppAsyncInternalLSP, blockCurrentMethodHook());
    }
    private static MethodHookFactory packageKillGateFactory() {
        return new MethodHookFactory() {
            @Override
            public IHook create(final Method method) {
                return new IHook() {
                    @Override
                    public void before() {
                        if (isAllowedPackageKill(this, method)) return;
                        setResult(defaultValue(method.getReturnType()));
                    }
                };
            }
        };
    }
    private static MethodHookFactory forceNoKillProcessFlagHook() {
        return new MethodHookFactory() {
            @Override
            public IHook create(final Method method) {
                final int argIndex = firstBooleanParameter(method, 1);
                return new IHook() {
                    @Override
                    public void before() {
                        if (argIndex >= 0) setArg(argIndex, false);
                    }
                };
            }
        };
    }
    private static MethodHookFactory blockCurrentMethodHook() {
        return new MethodHookFactory() {
            @Override
            public IHook create(final Method method) {
                return new IHook() {
                    @Override
                    public void before() {
                        setResult(defaultValue(method.getReturnType()));
                    }
                };
            }
        };
    }
    private static Object defaultValue(Class<?> type) {
        if (type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return (char) 0;
        return null;
    }
    private static boolean isAllowedPackageKill(IHook hook, Method method) {
        Class<?>[] types = method.getParameterTypes();
        int count = types.length;
        String description = lastStringArg(hook, types);
        if (count >= 3
            && types[count - 3] == int.class
            && types[count - 2] == int.class
            && types[count - 1] == String.class) {
            int reason = intArg(hook, count - 3, Integer.MIN_VALUE);
            int subReason = intArg(hook, count - 2, Integer.MIN_VALUE);
            boolean uninstalling = count >= 4
                && types[count - 4] == boolean.class
                && isBooleanArg(hook, count - 4, false);
            if (uninstalling || isRealForceStop(reason, subReason)) return true;
            if (reason == EXIT_REASON_PERMISSION_CHANGE
                || reason == EXIT_REASON_USER_STOPPED
                || reason == EXIT_REASON_PACKAGE_STATE_CHANGE
                || reason == EXIT_REASON_PACKAGE_UPDATED
                || subReason == EXIT_SUBREASON_PACKAGE_UPDATE) return true;
            return looksLikePackageMaintenance(description);
        }
        if (count >= 2
            && types[count - 2] == int.class
            && types[count - 1] == String.class) {
            int reason = intArg(hook, count - 2, Integer.MIN_VALUE);
            boolean uninstalling = count >= 3
                && types[count - 3] == boolean.class
                && isBooleanArg(hook, count - 3, false);
            if (uninstalling || isAllowedMaintenanceReason(reason)) return true;
            if (reason == EXIT_REASON_USER_REQUESTED
                && hasLegacyForceStopFlags(hook, types)) return true;
        }
        return hasLegacyForceStopFlags(hook, types)
            || looksLikeExplicitForceStop(description)
            || looksLikePackageMaintenance(description);
    }
    private static boolean isRealForceStop(int reason, int subReason) {
        return reason == EXIT_REASON_USER_REQUESTED
            && subReason == EXIT_SUBREASON_FORCE_STOP;
    }
    private static boolean isAllowedMaintenanceReason(int reason) {
        return reason == EXIT_REASON_PERMISSION_CHANGE
            || reason == EXIT_REASON_USER_STOPPED
            || reason == EXIT_REASON_PACKAGE_STATE_CHANGE
            || reason == EXIT_REASON_PACKAGE_UPDATED;
    }
    private static boolean hasLegacyForceStopFlags(IHook hook, Class<?>[] types) {
        if (types.length < 10
            || types[0] != String.class
            || types[1] != int.class
            || types[2] != int.class
            || types[3] != int.class) return false;
        for (int i = 4; i <= 9; i++) {
            if (types[i] != boolean.class) return false;
        }
        boolean allowRestart = isBooleanArg(hook, 5, true);
        boolean doit = isBooleanArg(hook, 6, false);
        boolean setRemoved = isBooleanArg(hook, 8, false);
        boolean uninstalling = isBooleanArg(hook, 9, false);
        return !allowRestart && doit && setRemoved && !uninstalling;
    }
    private static boolean looksLikePackageMaintenance(String description) {
        if (description == null) return false;
        String lower = description.toLowerCase(Locale.ROOT);
        return lower.contains("uninstall")
            || lower.contains("install")
            || lower.contains("update")
            || lower.contains("package removed")
            || lower.contains("remove package")
            || lower.contains("pkg removed")
            || lower.contains("package changed")
            || lower.contains("component state")
            || lower.contains("permission change")
            || lower.contains("user stopped")
            || lower.contains("stopping user");
    }
    private static boolean looksLikeExplicitForceStop(String description) {
        if (description == null) return false;
        String lower = description.toLowerCase(Locale.ROOT);
        return lower.contains("force stop")
            || lower.contains("force-stop")
            || lower.contains("force_stop");
    }
    private static int intArg(IHook hook, int index, int fallback) {
        Object arg = hook.getArg(index);
        return arg instanceof Integer ? (Integer) arg : fallback;
    }
    private static boolean isBooleanArg(IHook hook, int index, boolean fallback) {
        Object arg = hook.getArg(index);
        return arg instanceof Boolean ? (Boolean) arg : fallback;
    }
    private static String lastStringArg(IHook hook, Class<?>[] types) {
        for (int i = types.length - 1; i >= 0; i--) {
            if (types[i] != String.class) continue;
            Object arg = hook.getArg(i);
            if (arg instanceof String) return (String) arg;
        }
        return null;
    }
    private static int firstBooleanParameter(Method method, int start) {
        Class<?>[] types = method.getParameterTypes();
        for (int i = Math.max(0, start); i < types.length; i++) {
            if (types[i] == boolean.class) return i;
        }
        return -1;
    }
    private interface MethodHookFactory {
        IHook create(Method method);
    }
    private static void hookAllIfExists(String className, String methodName, MethodHookFactory callbackFactory) {
        try {
            Class<?> targetClass = findClass(className);
            for (Method method : findAllMethod(targetClass, methodName)) {
                if (method != null) {
                    hook(method, callbackFactory.create(method));
                }
            }
        } catch (Throwable t) {
            logW(TAG, "Skip hook-all " + className + "#" + methodName + ": " + t);
        }
    }
}
