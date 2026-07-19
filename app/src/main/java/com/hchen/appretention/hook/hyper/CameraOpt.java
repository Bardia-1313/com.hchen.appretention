package com.hchen.appretention.hook.hyper;
import static com.hchen.appretention.data.method.HyperMethod.boostCameraByThreshold;
import static com.hchen.appretention.data.method.HyperMethod.callMethod;
import static com.hchen.appretention.data.method.HyperMethod.callStaticMethod;
import static com.hchen.appretention.data.method.HyperMethod.doAdjBoost;
import static com.hchen.appretention.data.method.HyperMethod.interceptAppRestartIfNeeded;
import static com.hchen.appretention.data.method.HyperMethod.isAllowAdjBoost;
import static com.hchen.appretention.data.method.HyperMethod.newInstance;
import static com.hchen.appretention.data.method.HyperMethod.notifyActivityChanged;
import static com.hchen.appretention.data.method.HyperMethod.notifyCameraForegroundChange;
import static com.hchen.appretention.data.method.HyperMethod.notifyCameraForegroundState;
import static com.hchen.appretention.data.method.HyperMethod.notifyCameraPostProcessState;
import static com.hchen.appretention.data.method.HyperMethod.reclaimMemoryForCamera;
import static com.hchen.appretention.data.method.HyperMethod.updateCameraBoosterCloudData;
import static com.hchen.appretention.data.path.HyperClass.CameraOpt;
import static com.hchen.appretention.data.path.HyperClass.ICameraBooster;
import static com.hchen.appretention.data.path.HyperClass.ICameraBooster$CameraBoosterProxy;
import static com.hchen.appretention.data.path.HyperClass.ProcessManagerInternal;
import static com.hchen.appretention.data.path.HyperClass.ServiceThread;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.prop.SystemProp.ZERO;
import static com.hchen.hooktool.core.CoreTool.doNothing;
import static com.hchen.hooktool.core.CoreTool.existsAnyMethod;
import static com.hchen.hooktool.core.CoreTool.existsClass;
import static com.hchen.hooktool.core.CoreTool.existsField;
import static com.hchen.hooktool.core.CoreTool.findAllMethod;
import static com.hchen.hooktool.core.CoreTool.findClass;
import static com.hchen.hooktool.core.CoreTool.hook;
import static com.hchen.hooktool.core.CoreTool.hookMethod;
import static com.hchen.hooktool.core.CoreTool.returnResult;
import android.content.Context;
import com.hchen.appretention.data.field.HyperField;
import com.hchen.hooktool.core.CoreTool;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.utils.SystemPropTool;
import java.lang.reflect.Method;
public class CameraOpt {
    public static void doHook() {
        SystemPropTool.setProp("persist.sys.lmkd.extend_reclaim.enable", ZERO);
        SystemPropTool.setProp("persist.sys.lmkd.double_watermark.enable", ZERO);
        SystemPropTool.setProp("persist.sys.lmkd.camera_adaptive_lmk.enable", ZERO);
        SystemPropTool.setProp("persist.sys.lmk.camera.mem_reclaim", ZERO);
        SystemPropTool.setProp("persist.sys.miui.camera.boost.enable", ZERO);
        SystemPropTool.setProp("persist.sys.miui.camera.boost.opt", ZERO);
        SystemPropTool.setProp("persist.sys.miui.camera.boost.killAdj_threshold", "1001");
        if (existsClass(CameraOpt)) {
            Class<?> mCameraOpt = findClass(CameraOpt);
            if (existsField(mCameraOpt, HyperField.mCameraBoosterClazz) || existsField(mCameraOpt, HyperField.mQuickCameraClazz)) {
                hookMethod(CameraOpt,
                    callStaticMethod,
                    Class.class, String.class, Object[].class,
                    returnResult(null)
                );
            } else {
                hookMethod(CameraOpt,
                    callMethod,
                    String.class, Object[].class,
                    returnResult(null)
                );
            }
        } else if (existsClass(ICameraBooster)) {
            hookMethod(ICameraBooster,
                newInstance,
                ProcessManagerInternal, ActivityManagerService, ServiceThread, Context.class,
                new IHook() {
                    @Override
                    public void before() {
                        Object mCameraBoosterProxy = CoreTool.newInstance(ICameraBooster$CameraBoosterProxy);
                        setResult(mCameraBoosterProxy);
                    }
                    @Override
                    public void after() {
                    }
                }
            );
        }
    }
    @Deprecated 
    private static void doHookCameraOpt(Class<?> cameraBooster) {
        String[] mCameraOptShouldHookMethodList = new String[]{
            boostCameraByThreshold,
            doAdjBoost, 
            interceptAppRestartIfNeeded, 
            isAllowAdjBoost, 
            notifyCameraForegroundChange,
            notifyCameraForegroundState,
            notifyCameraPostProcessState,
            notifyActivityChanged,
            reclaimMemoryForCamera, 
            updateCameraBoosterCloudData 
        };
        for (String m : mCameraOptShouldHookMethodList) {
            if (existsAnyMethod(cameraBooster, m)) {
                Method method = findAllMethod(cameraBooster, m)[0];
                if (method == null) continue;
                if (method.getName().equals(interceptAppRestartIfNeeded)) {
                    hook(method, returnResult(false));
                } else if (isAllowAdjBoost.equals(method.getName())) {
                    hook(method, returnResult(true));
                } else
                    hook(method, doNothing());
            }
        }
    }
}
