package com.hchen.appretention.hook.xiaomi;
import static com.hchen.appretention.log.AppRetentionXposedLog.logD;
import static com.hchen.appretention.log.AppRetentionXposedLog.logW;
import static com.hchen.hooktool.core.CoreTool.findAllMethod;
import static com.hchen.hooktool.core.CoreTool.findClass;
import static com.hchen.hooktool.core.CoreTool.hook;
import android.content.ComponentName;
import android.content.Intent;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.utils.SystemPropTool;
import java.lang.reflect.Method;
import java.util.Locale;
public final class OemProcessProtection {
    private static final String TAG = "OemProcessProtection";
    private static final String PROP_ENABLE = "persist.hchen.retention.oem_kill_gate";
    private static final String PROP_BLOCK_EXTERNAL_SERVICE_STOP =
        "persist.hchen.retention.block_external_service_stop";
    private static final String ACTIVITY_MANAGER = "android.app.ActivityManager";
    private static final String ACTIVITY_MANAGER_PROXY = "android.app.IActivityManager$Stub$Proxy";
    private static final String CONTEXT_IMPL = "android.app.ContextImpl";
    private static final String PROCESS_MANAGER = "miui.process.ProcessManager";
    private OemProcessProtection() {
    }
    public static void installPowerKeeper() {
        if (!installCommon("com.miui.powerkeeper", true)) return;
        blockAll("com.miui.powerkeeper.utils.ProcessUtils", "killProcess");
        blockAll("com.miui.powerkeeper.utils.ProcessUtils", "killCameraProcess");
        hookNativeKillGateway("com.miui.powerkeeper.utils.OctVmNativeProxy",
            "sudebug_command_execute");
        hookNativeKillGateway("com.miui.powerkeeper.utils.OctVmNativeProxy",
            "sudebug_camera_command_execute");
    }
    public static void installSecurityCenter() {
        if (!installCommon("com.miui.securitycenter", false)) return;
        blockAll("com.miui.securitycenter.memory.MemoryCheck", "B4");
        blockAll("ge.c", "g");
        blockAll("f4.a1", "M");
    }
    private static boolean installCommon(String ownerPackage, boolean blockForceStopApis) {
        if (!SystemPropTool.getProp(PROP_ENABLE, true)) {
            logD(TAG, "OEM kill gate is disabled for " + ownerPackage);
            return false;
        }
        blockAll(PROCESS_MANAGER, "kill");
        blockAll(ACTIVITY_MANAGER, "killBackgroundProcesses");
        blockAll(ACTIVITY_MANAGER_PROXY, "killPids");
        blockAll(ACTIVITY_MANAGER_PROXY, "killBackgroundProcesses");
        if (blockForceStopApis) {
            blockAll(ACTIVITY_MANAGER, "killApplicationProcess");
            blockAll(ACTIVITY_MANAGER, "killUid");
            blockAll(ACTIVITY_MANAGER, "forceStopPackage");
            blockAll(ACTIVITY_MANAGER, "forceStopPackageAsUser");
            blockAll(ACTIVITY_MANAGER, "restartPackage");
            blockAll(ACTIVITY_MANAGER_PROXY, "killApplication");
            blockAll(ACTIVITY_MANAGER_PROXY, "killApplicationProcess");
            blockAll(ACTIVITY_MANAGER_PROXY, "killUid");
            blockAll(ACTIVITY_MANAGER_PROXY, "forceStopPackage");
        }
        if (SystemPropTool.getProp(PROP_BLOCK_EXTERNAL_SERVICE_STOP, true)) {
            hookExternalServiceStop(ownerPackage, "stopService");
            hookExternalServiceStop(ownerPackage, "stopServiceAsUser");
        }
        return true;
    }
    private static void blockAll(String className, String methodName) {
        try {
            Class<?> targetClass = findClass(className);
            for (Method method : findAllMethod(targetClass, methodName)) {
                if (method == null) continue;
                hook(method, new IHook() {
                    @Override
                    public void before() {
                        setResult(defaultValue(method.getReturnType()));
                    }
                });
            }
        } catch (Throwable t) {
            logD(TAG, "Optional hook unavailable: " + className + "#" + methodName);
        }
    }
    private static void hookNativeKillGateway(String className, String methodName) {
        try {
            Class<?> targetClass = findClass(className);
            for (Method method : findAllMethod(targetClass, methodName)) {
                if (method == null || method.getParameterCount() == 0) continue;
                hook(method, new IHook() {
                    @Override
                    public void before() {
                        Object arg = getArg(0);
                        if (!(arg instanceof String[])) return;
                        for (String token : (String[]) arg) {
                            if (token == null) continue;
                            String normalized = token.toLowerCase(Locale.ROOT);
                            if (normalized.contains("kill_process")
                                || normalized.contains("kill_camera_process")) {
                                setResult(defaultValue(method.getReturnType()));
                                return;
                            }
                        }
                    }
                });
            }
        } catch (Throwable t) {
            logD(TAG, "Optional native gateway unavailable: " + className + "#" + methodName);
        }
    }
    private static void hookExternalServiceStop(String ownerPackage, String methodName) {
        try {
            Class<?> targetClass = findClass(CONTEXT_IMPL);
            for (Method method : findAllMethod(targetClass, methodName)) {
                if (method == null) continue;
                final int intentIndex = findIntentParameter(method);
                if (intentIndex < 0) continue;
                hook(method, new IHook() {
                    @Override
                    public void before() {
                        Object arg = getArg(intentIndex);
                        if (!(arg instanceof Intent)) return;
                        Intent intent = (Intent) arg;
                        ComponentName component = intent.getComponent();
                        String targetPackage = component != null
                            ? component.getPackageName() : intent.getPackage();
                        if (targetPackage == null || ownerPackage.equals(targetPackage)) return;
                        logD(TAG, "Blocked external stopService from " + ownerPackage
                            + " to " + targetPackage);
                        setResult(defaultValue(method.getReturnType()));
                    }
                });
            }
        } catch (Throwable t) {
            logW(TAG, "Unable to install external service-stop gate for " + ownerPackage + ": " + t);
        }
    }
    private static int findIntentParameter(Method method) {
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (Intent.class.isAssignableFrom(types[i])) return i;
        }
        return -1;
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
}
