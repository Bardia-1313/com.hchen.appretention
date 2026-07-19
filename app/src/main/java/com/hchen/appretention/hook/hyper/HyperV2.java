package com.hchen.appretention.hook.hyper;
import static com.hchen.appretention.data.field.HyperField.IS_ENABLE_RECLAIM;
import static com.hchen.appretention.data.field.HyperField.PROCESS_CLEANER_ENABLED;
import static com.hchen.appretention.data.field.HyperField.PROCESS_TRACKER_ENABLE;
import static com.hchen.appretention.data.field.HyperField.PROC_CPU_EXCEPTION_ENABLE;
import static com.hchen.appretention.data.field.HyperField.RECLAIM_IF_NEEDED;
import static com.hchen.appretention.data.field.HyperField.sCompactSingleProcEnable;
import static com.hchen.appretention.data.field.HyperField.sCompactionEnable;
import static com.hchen.appretention.data.method.HyperMethod.SetdmoptEnable;
import static com.hchen.appretention.data.method.HyperMethod.addMiuiPeriodicCleanerService;
import static com.hchen.appretention.data.method.HyperMethod.getBackgroundAppCount;
import static com.hchen.appretention.data.method.HyperMethod.getDeviceLevelForRAM;
import static com.hchen.appretention.data.method.HyperMethod.handleAutoLockOff;
import static com.hchen.appretention.data.method.HyperMethod.handleKillAll;
import static com.hchen.appretention.data.method.HyperMethod.handleKillApp;
import static com.hchen.appretention.data.method.HyperMethod.handleLimitCpuException;
import static com.hchen.appretention.data.method.HyperMethod.handleThermalKillProc;
import static com.hchen.appretention.data.method.HyperMethod.isEnable;
import static com.hchen.appretention.data.method.HyperMethod.isMiuiLiteVersion;
import static com.hchen.appretention.data.method.HyperMethod.isSSModelEnable;
import static com.hchen.appretention.data.method.HyperMethod.killAppExceedingHeapThreshold;
import static com.hchen.appretention.data.method.HyperMethod.killBackgroundApps;
import static com.hchen.appretention.data.method.HyperMethod.killPackage;
import static com.hchen.appretention.data.method.HyperMethod.killProcess;
import static com.hchen.appretention.data.method.HyperMethod.killProcessByMinAdj;
import static com.hchen.appretention.data.method.HyperMethod.nStartPressureMonitor;
import static com.hchen.appretention.data.method.HyperMethod.onStartJob;
import static com.hchen.appretention.data.method.HyperMethod.performCompaction;
import static com.hchen.appretention.data.method.HyperMethod.preloadAppEnqueue;
import static com.hchen.appretention.data.method.HyperMethod.reclaimBackground;
import static com.hchen.appretention.data.method.HyperMethod.scanProcessAndCleanUpMemory;
import static com.hchen.appretention.data.method.HyperMethod.updateScreenState;
import static com.hchen.appretention.data.path.HyperClass.ActivityTaskManagerService;
import static com.hchen.appretention.data.path.HyperClass.Build;
import static com.hchen.appretention.data.path.HyperClass.ExtendMImpl;
import static com.hchen.appretention.data.path.HyperClass.GameMemoryCleanerDeprecated;
import static com.hchen.appretention.data.path.HyperClass.GameMemoryReclaimer;
import static com.hchen.appretention.data.path.HyperClass.IAppState$IRunningProcess;
import static com.hchen.appretention.data.path.HyperClass.LifecycleConfig;
import static com.hchen.appretention.data.path.HyperClass.MemoryFreezeStubImpl;
import static com.hchen.appretention.data.path.HyperClass.MemoryStandardProcessControl;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemReclaimer;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemoryService;
import static com.hchen.appretention.data.path.HyperClass.OomAdjusterImpl;
import static com.hchen.appretention.data.path.HyperClass.PreloadAppControllerImpl;
import static com.hchen.appretention.data.path.HyperClass.PressureStateSettings;
import static com.hchen.appretention.data.path.HyperClass.ProcessConfig;
import static com.hchen.appretention.data.path.HyperClass.ProcessKillerIdler;
import static com.hchen.appretention.data.path.HyperClass.ProcessMemoryCleaner;
import static com.hchen.appretention.data.path.HyperClass.ProcessPowerCleaner;
import static com.hchen.appretention.data.path.HyperClass.SlowStartupSceneMemClean;
import static com.hchen.appretention.data.path.HyperClass.SmartCpuPolicyManager;
import static com.hchen.appretention.data.path.HyperClass.SystemPressureController;
import static com.hchen.appretention.data.path.HyperClass.SystemPressureControllerNative;
import static com.hchen.appretention.data.path.HyperClass.SystemServerImpl;
import static com.hchen.appretention.data.prop.SystemProp.FALSE;
import static com.hchen.appretention.data.prop.SystemProp.ONE;
import static com.hchen.appretention.data.prop.SystemProp.TRUE;
import android.app.job.JobParameters;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.utils.SystemPropTool;
import java.util.List;
@HookEntrance(targetBrand = "Xiaomi", targetPackage = "android", targetOS = 2.0f, isHyperOS = true)
public class HyperV2 extends HCBase {
    @Override
    public void init() {
        SystemPropTool.setProp("persist.sys.prestart.proc", FALSE);
        SystemPropTool.setProp("persist.sys.spc.enabled", FALSE);
        SystemPropTool.setProp("persist.sys.spc.cpuexception.enable", FALSE);
        SystemPropTool.setProp("persist.sys.spc.process.tracker.enable", FALSE);
        setStaticField(PressureStateSettings, PROCESS_CLEANER_ENABLED, false);
        setStaticField(PressureStateSettings, PROC_CPU_EXCEPTION_ENABLE, false);
        setStaticField(PressureStateSettings, PROCESS_TRACKER_ENABLE, false);
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
        hookMethod(OomAdjusterImpl,
            getBackgroundAppCount,
            returnResult(100)
        );
        SystemPropTool.setProp("persist.sys.periodic.u.enable", FALSE);
        SystemPropTool.setProp("persist.sys.periodic.u.startprocess.enable", FALSE);
        hookMethod(SystemServerImpl,
            addMiuiPeriodicCleanerService,
            ActivityTaskManagerService,
            doNothing()
        );
        if (existsMethod(ExtendMImpl, SetdmoptEnable)) {
            SystemPropTool.setProp("persist.miui.extm.enable", ONE);
            SystemPropTool.setProp("persist.miui.extm.dm_opt.enable", TRUE);
            hookMethod(ExtendMImpl,
                SetdmoptEnable,
                doNothing()
            );
        }
        SystemPropTool.setProp("persist.sys.mfz.enable", FALSE);
        hookMethod(MemoryFreezeStubImpl,
            isEnable,
            returnResult(false)
        );
        SystemPropTool.setProp("persist.sys.memory_standard.enable", FALSE);
        SystemPropTool.setProp("persist.sys.memory_standard.appheap.enable", FALSE);
        buildChain(MemoryStandardProcessControl)
            .findMethod(isEnable)
            .returnResult(false);
        buildChain(ProcessPowerCleaner)
            .findMethod(handleThermalKillProc, ProcessConfig)
            .doNothing()
            .findMethod(handleKillAll, ProcessConfig, boolean.class)
            .doNothing()
            .findMethod(handleKillApp, ProcessConfig)
            .returnResult(true)
            .findMethod(handleAutoLockOff).doNothing();
        buildChain(ProcessMemoryCleaner)
            .findMethod(scanProcessAndCleanUpMemory, long.class) 
            .returnResult(true)
            .findMethod(killPackage, IAppState$IRunningProcess, int.class, String.class)
            .returnResult(0L)
            .findMethod(killProcess, IAppState$IRunningProcess, int.class, String.class)
            .returnResult(0L)
            .findMethod(killProcessByMinAdj, int.class, String.class, List.class)
            .doNothing()
            .findMethod(killAppExceedingHeapThreshold, int.class)
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
        hookMethod(GameMemoryCleanerDeprecated,
            killBackgroundApps,
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
        SystemPropTool.setProp("persist.sys.ssmc.enable", FALSE);
        hookMethodIfExists(SlowStartupSceneMemClean, isSSModelEnable, returnResult(false));
        SystemPropTool.setProp("persist.sys.miui.damon.enable", FALSE);
        SystemPropTool.setProp("persist.sys.miui.damon.reclaim.enable", FALSE);
        setStaticField(SystemPressureController, IS_ENABLE_RECLAIM, false);
        buildChain(SystemPressureController)
            .findMethod(updateScreenState, boolean.class)
            .doNothing()
            .findMethodIfExist(nStartPressureMonitor)
            .hook(doNothing());
        if (existsClass(SystemPressureControllerNative)) {
            hookMethodIfExists(SystemPressureControllerNative,
                nStartPressureMonitor,
                doNothing()
            );
        }
        CameraOpt.doHook();
    }
}
