package com.hchen.appretention.hook.miui;
import static com.hchen.appretention.data.field.HyperField.IS_ENABLE_RECLAIM;
import static com.hchen.appretention.data.field.HyperField.PROCESS_CLEANER_ENABLED;
import static com.hchen.appretention.data.field.HyperField.PROC_CPU_EXCEPTION_ENABLE;
import static com.hchen.appretention.data.field.HyperField.RECLAIM_IF_NEEDED;
import static com.hchen.appretention.data.field.HyperField.sCompactSingleProcEnable;
import static com.hchen.appretention.data.field.HyperField.sCompactionEnable;
import static com.hchen.appretention.data.method.HyperMethod.cleanUpMemory;
import static com.hchen.appretention.data.method.HyperMethod.doClean;
import static com.hchen.appretention.data.method.HyperMethod.getDeviceLevelForRAM;
import static com.hchen.appretention.data.method.HyperMethod.handleAutoLockOff;
import static com.hchen.appretention.data.method.HyperMethod.handleKillAll;
import static com.hchen.appretention.data.method.HyperMethod.handleKillApp;
import static com.hchen.appretention.data.method.HyperMethod.handleLimitCpuException;
import static com.hchen.appretention.data.method.HyperMethod.handleScreenOff;
import static com.hchen.appretention.data.method.HyperMethod.handleThermalKillProc;
import static com.hchen.appretention.data.method.HyperMethod.isMiuiLiteVersion;
import static com.hchen.appretention.data.method.HyperMethod.killPackage;
import static com.hchen.appretention.data.method.HyperMethod.killProcess;
import static com.hchen.appretention.data.method.HyperMethod.killProcessByMinAdj;
import static com.hchen.appretention.data.method.HyperMethod.nStartPressureMonitor;
import static com.hchen.appretention.data.method.HyperMethod.onStartJob;
import static com.hchen.appretention.data.method.HyperMethod.performCompaction;
import static com.hchen.appretention.data.method.HyperMethod.preloadAppEnqueue;
import static com.hchen.appretention.data.method.HyperMethod.reclaimBackground;
import static com.hchen.appretention.data.method.HyperMethod.updateScreenState;
import static com.hchen.appretention.data.path.HyperClass.AppStateManager$AppState$RunningProcess;
import static com.hchen.appretention.data.path.HyperClass.Build;
import static com.hchen.appretention.data.path.HyperClass.GameMemoryReclaimer;
import static com.hchen.appretention.data.path.HyperClass.LifecycleConfig;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemReclaimer;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemoryService;
import static com.hchen.appretention.data.path.HyperClass.PeriodicCleanerService;
import static com.hchen.appretention.data.path.HyperClass.PreloadAppControllerImpl;
import static com.hchen.appretention.data.path.HyperClass.PressureStateSettings;
import static com.hchen.appretention.data.path.HyperClass.ProcessConfig;
import static com.hchen.appretention.data.path.HyperClass.ProcessKillerIdler;
import static com.hchen.appretention.data.path.HyperClass.ProcessMemoryCleaner;
import static com.hchen.appretention.data.path.HyperClass.ProcessPowerCleaner;
import static com.hchen.appretention.data.path.HyperClass.SmartCpuPolicyManager;
import static com.hchen.appretention.data.path.HyperClass.SystemPressureController;
import static com.hchen.appretention.data.prop.SystemProp.FALSE;
import android.app.job.JobParameters;
import com.hchen.appretention.hook.hyper.CameraOpt;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.utils.SystemPropTool;
import java.util.List;
@HookEntrance(targetBrand = "Xiaomi", targetPackage = "android", targetOS = 14f)
public class MiuiV14 extends HCBase {
    @Override
    public void init() {
        SystemPropTool.setProp("persist.sys.spc.enabled", FALSE);
        SystemPropTool.setProp("persist.sys.spc.cpuexception.enable", FALSE);
        setStaticField(PressureStateSettings, PROCESS_CLEANER_ENABLED, false);
        setStaticField(PressureStateSettings, PROC_CPU_EXCEPTION_ENABLE, false);
        hookMethod(GameMemoryReclaimer,
            reclaimBackground,
            long.class,
            doNothing()
        );
        hookMethod(Build,
            isMiuiLiteVersion,
            returnResult(false)
        );
        hookMethod(Build,
            getDeviceLevelForRAM,
            int.class,
            returnResult(3)
        );
        SystemPropTool.setProp("persist.sys.periodic.enable", FALSE);
        hookMethod(PeriodicCleanerService,
            handleScreenOff,
            doNothing()
        );
        hookMethod(PeriodicCleanerService,
            doClean,
            int.class, int.class, int.class, String.class,
            doNothing()
        );
        setStaticField(SystemPressureController, IS_ENABLE_RECLAIM, false);
        buildChain(SystemPressureController)
            .findMethod(updateScreenState, boolean.class)
            .doNothing()
            .findMethod(nStartPressureMonitor)
            .doNothing();
        buildChain(ProcessPowerCleaner)
            .findMethod(handleThermalKillProc, ProcessConfig)
            .doNothing()
            .findMethod(handleKillAll, ProcessConfig, boolean.class)
            .doNothing()
            .findMethod(handleKillApp, ProcessConfig)
            .returnResult(true)
            .findMethod(handleAutoLockOff)
            .doNothing();
        SystemPropTool.setProp("persist.sys.mms.compact_enable", FALSE);
        SystemPropTool.setProp("persist.sys.mms.single_compact_enable", FALSE);
        setStaticField(MiuiMemReclaimer, RECLAIM_IF_NEEDED, false);
        setStaticField(MiuiMemoryService, sCompactionEnable, false);
        setStaticField(MiuiMemoryService, sCompactSingleProcEnable, false);
        hookMethod(MiuiMemReclaimer,
            performCompaction,
            String.class, int.class,
            doNothing()
        );
        hookMethod(SmartCpuPolicyManager,
            handleLimitCpuException,
            int.class,
            doNothing()
        );
        hookMethod(ProcessKillerIdler,
            onStartJob,
            JobParameters.class,
            returnResult(false)
        );
        buildChain(PreloadAppControllerImpl)
            .findMethod(preloadAppEnqueue, String.class, boolean.class, LifecycleConfig)
            .doNothing();
        buildChain(ProcessMemoryCleaner)
            .findMethod(cleanUpMemory, List.class, long.class)
            .returnResult(true)
            .findMethod(killPackage, AppStateManager$AppState$RunningProcess, int.class, String.class)
            .returnResult(0L)
            .findMethod(killProcess, AppStateManager$AppState$RunningProcess, int.class, String.class)
            .returnResult(0L)
            .findMethod(killProcessByMinAdj, int.class, String.class, List.class)
            .doNothing();
        CameraOpt.doHook();
    }
}
