package com.hchen.appretention.hook.system;
import static com.hchen.appretention.data.field.SystemField.CUR_MAX_CACHED_PROCESSES;
import static com.hchen.appretention.data.field.SystemField.MAX_PHANTOM_PROCESSES;
import static com.hchen.appretention.data.field.SystemField.USE_MODERN_TRIM;
import static com.hchen.appretention.data.field.SystemField.mGlobalMaxNumTasks;
import static com.hchen.appretention.data.field.SystemField.mKillBgRestrictedAndCachedIdle;
import static com.hchen.appretention.data.field.SystemField.mMemFactorOverride;
import static com.hchen.appretention.data.field.SystemField.mMinNumVisibleTasks;
import static com.hchen.appretention.data.field.SystemField.mNextNoKillDebugMessageTime;
import static com.hchen.appretention.data.method.SystemMethod.checkExcessivePowerUsageLPr;
import static com.hchen.appretention.data.method.SystemMethod.isInVisibleRange;
import static com.hchen.appretention.data.method.SystemMethod.killProcessesWhenImperceptible;
import static com.hchen.appretention.data.method.SystemMethod.performIdleMaintenance;
import static com.hchen.appretention.data.method.SystemMethod.shouldKillExcessiveProcesses;
import static com.hchen.appretention.data.method.SystemMethod.trimInactiveRecentTasks;
import static com.hchen.appretention.data.method.SystemMethod.trimPhantomProcessesIfNecessary;
import static com.hchen.appretention.data.method.SystemMethod.updateAndTrimProcessLSP;
import static com.hchen.appretention.data.method.SystemMethod.updateKillBgRestrictedCachedIdle;
import static com.hchen.appretention.data.method.SystemMethod.updateMaxCachedProcesses;
import static com.hchen.appretention.data.method.SystemMethod.updateMaxPhantomProcesses;
import static com.hchen.appretention.data.method.SystemMethod.updatePerfConfigConstants;
import static com.hchen.appretention.data.method.SystemMethod.updateProcessCpuStatesLocked;
import static com.hchen.appretention.data.method.SystemMethod.updateUseModernTrim;
import static com.hchen.appretention.data.path.SystemClass.ActiveUids;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerConstants;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.AppProfiler;
import static com.hchen.appretention.data.path.SystemClass.LowMemDetector;
import static com.hchen.appretention.data.path.SystemClass.OomAdjuster;
import static com.hchen.appretention.data.path.SystemClass.PhantomProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessCpuTracker;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessRecord;
import static com.hchen.appretention.data.path.SystemClass.RecentTasks;
import static com.hchen.appretention.data.path.SystemClass.Task;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.hchen.appretention.hook.system.opt.ApplyAdjOpt;
import com.hchen.appretention.hook.system.opt.CacheCompaction;
import com.hchen.appretention.hook.system.opt.ForceStopOnlyPolicy;
import com.hchen.appretention.hook.system.opt.OomLevelsOpt;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;
@HookEntrance(targetPackage = "android", targetSdks = 33)
public class AndroidT extends HCBase {
    @Override
    public void init() {
        OomLevelsOpt.init();
        CacheCompaction.init();
        ApplyAdjOpt.init();
        ForceStopOnlyPolicy.init();
        hookMethod(ProcessList,
            killProcessesWhenImperceptible,
            int[].class, String.class, int.class,
            doNothing()
        );
        hookMethod(PhantomProcessList,
            updateProcessCpuStatesLocked,
            ProcessCpuTracker,
            doNothing()
        );
        hookMethod(PhantomProcessList,
            trimPhantomProcessesIfNecessary,
            doNothing()
        );
        hookMethod(ActivityManagerService,
            checkExcessivePowerUsageLPr,
            long.class, boolean.class, long.class,
            String.class, String.class, int.class,
            ProcessRecord,
            returnResult(false)
        );
        hookMethod(ActivityManagerService,
            performIdleMaintenance,
            doNothing()
        );
        hookConstructor(AppProfiler,
            ActivityManagerService, Looper.class, LowMemDetector,
            new IHook() {
                @Override
                public void after() {
                    setThisField(mMemFactorOverride, 0);
                }
            }
        );
        hookMethod(OomAdjuster,
            shouldKillExcessiveProcesses,
            long.class,
            returnResult(false)
        );
        hookMethod(OomAdjuster,
            updateAndTrimProcessLSP,
            long.class, long.class, long.class,
            ActiveUids, 
            new IHook() {
                @Override
                public void before() {
                    setThisField(mNextNoKillDebugMessageTime, Long.MAX_VALUE); 
                }
            }
        );
        hookMethod(RecentTasks,
            trimInactiveRecentTasks,
            new IHook() {
                @Override
                public void before() {
                    setThisField(mGlobalMaxNumTasks, Integer.MAX_VALUE);
                }
            }
        );
        hookMethod(RecentTasks,
            isInVisibleRange,
            Task, int.class, int.class, boolean.class,
            new IHook() {
                @Override
                public void before() {
                    setThisField(mMinNumVisibleTasks, Integer.MAX_VALUE);
                }
            }
        );
        buildChain(ActivityManagerConstants)
            .findConstructor(
                Context.class, ActivityManagerService, Handler.class)
            .hook(new IHook() {
                @Override
                public void after() {
                    setThisField(CUR_MAX_CACHED_PROCESSES, 6144); 
                    setThisField(MAX_PHANTOM_PROCESSES, Integer.MAX_VALUE); 
                    setThisField(mKillBgRestrictedAndCachedIdle, false); 
                    if (existsField(ActivityManagerConstants, USE_MODERN_TRIM))
                        setThisField(USE_MODERN_TRIM, true); 
                }
            })
            .findMethod(updateKillBgRestrictedCachedIdle)
            .doNothing()
            .findMethodIfExist(updateUseModernTrim) 
            .doNothing()
            .findMethod(updateMaxCachedProcesses)
            .doNothing()
            .findMethod(updateMaxPhantomProcesses)
            .doNothing()
            .findMethodIfExist(updatePerfConfigConstants) 
            .doNothing();
    }
}
