package com.hchen.appretention.hook.samsung;
import static com.hchen.appretention.data.field.OneUiField.ENABLE_KILL_LONG_RUNNING_PROCESS;
import static com.hchen.appretention.data.field.OneUiField.INSTANCE;
import static com.hchen.appretention.data.field.OneUiField.KPM_BTIME_ENABLE;
import static com.hchen.appretention.data.field.OneUiField.KPM_POLICY_ENABLE;
import static com.hchen.appretention.data.field.OneUiField.MARs_ENABLE;
import static com.hchen.appretention.data.field.OneUiField.MAX_LONG_LIVE_APP;
import static com.hchen.appretention.data.field.OneUiField.WRITEBACK_ENABLED;
import static com.hchen.appretention.data.method.OneUiMethod.IsForceKillHeavyProcess;
import static com.hchen.appretention.data.method.OneUiMethod.activeLaunchKillCheck;
import static com.hchen.appretention.data.method.OneUiMethod.addLongLivePackageLocked;
import static com.hchen.appretention.data.method.OneUiMethod.checkKeptProcess;
import static com.hchen.appretention.data.method.OneUiMethod.getInstance;
import static com.hchen.appretention.data.method.OneUiMethod.getMARsEnabled;
import static com.hchen.appretention.data.method.OneUiMethod.getMaxLongLiveApps;
import static com.hchen.appretention.data.method.OneUiMethod.isBEKCondition;
import static com.hchen.appretention.data.method.OneUiMethod.isExcessiveResourceUsage;
import static com.hchen.appretention.data.method.OneUiMethod.isPmmEnabled;
import static com.hchen.appretention.data.method.OneUiMethod.killTimeOverEmptyProcess;
import static com.hchen.appretention.data.method.OneUiMethod.setLmkdCameraKillBoost;
import static com.hchen.appretention.data.method.OneUiMethod.setLmkdParameter;
import static com.hchen.appretention.data.method.OneUiMethod.updateNapProcessProtection;
import static com.hchen.appretention.data.path.OneUiClass.ActivityManagerServiceExt;
import static com.hchen.appretention.data.path.OneUiClass.BGProtectManager;
import static com.hchen.appretention.data.path.OneUiClass.ChimeraManagerService;
import static com.hchen.appretention.data.path.OneUiClass.DynamicHiddenApp;
import static com.hchen.appretention.data.path.OneUiClass.KillPolicyManager;
import static com.hchen.appretention.data.path.OneUiClass.MARsPolicyManager;
import static com.hchen.appretention.data.path.OneUiClass.PerProcessNandswap;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessRecord;
import android.content.Context;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;
@HookEntrance(targetPackage = "android", targetBrand = "samsung")
public class OneUi extends HCBase {
    @Override
    public void init() {
        LmkdParameter.init();
        LmkdParameter.forceReplace();
        hookMethod(ProcessList,
            setLmkdParameter,
            int.class, int.class,
            new IHook() {
                @Override
                public void before() {
                    LmkdParameter.replace(this);
                }
            }
        );
        hookMethod(ProcessList,
            setLmkdCameraKillBoost,
            int.class, int.class, int.class,
            doNothing()
        );
        hookConstructor(ChimeraManagerService,
            Context.class, ActivityManagerService,
            doNothing()
        );
        hookMethod(DynamicHiddenApp,
            activeLaunchKillCheck,
            ProcessRecord,
            doNothing()
        );
        hookMethod(DynamicHiddenApp,
            killTimeOverEmptyProcess,
            ProcessRecord, int.class, long.class,
            doNothing()
        );
        hookMethod(BGProtectManager,
            updateNapProcessProtection,
            ProcessRecord,
            doNothing()
        );
        hookMethod(BGProtectManager,
            IsForceKillHeavyProcess,
            String.class,
            returnResult(false)
        );
        hookMethod(BGProtectManager,
            isBEKCondition,
            ProcessRecord,
            returnResult(true)
        );
        hookMethod(BGProtectManager,
            checkKeptProcess,
            ProcessRecord,
            returnResult(0)
        );
        hookMethod(ProcessRecord,
            isExcessiveResourceUsage,
            returnResult(false)
        );
        hookMethod(PerProcessNandswap,
            getInstance,
            new IHook() {
                @Override
                public void after() {
                    Object perProcessNandswap = getStaticField(PerProcessNandswap, INSTANCE);
                    if (perProcessNandswap == null)
                        perProcessNandswap = getResult();
                    if (perProcessNandswap != null)
                        setField(perProcessNandswap, WRITEBACK_ENABLED, false);
                    logD(TAG, "PerProcessNandswap: " + perProcessNandswap);
                }
            }
        );
        buildChain(ActivityManagerServiceExt)
            .findMethod(addLongLivePackageLocked, String.class)
            .hook(new IHook() {
                @Override
                public void before() {
                    setStaticField(ActivityManagerServiceExt, MAX_LONG_LIVE_APP, Integer.MAX_VALUE);
                }
            })
            .findMethod(getMaxLongLiveApps)
            .returnResult(Integer.MAX_VALUE);
        setStaticField(MARsPolicyManager, ENABLE_KILL_LONG_RUNNING_PROCESS, false);
        setStaticField(MARsPolicyManager, MARs_ENABLE, false);
        hookMethod(MARsPolicyManager,
            getMARsEnabled,
            returnResult(false)
        );
        hookMethod(ActivityManagerService,
            isPmmEnabled,
            returnResult(false)
        );
        setStaticField(KillPolicyManager, KPM_POLICY_ENABLE, false);
        setStaticField(KillPolicyManager, KPM_BTIME_ENABLE, false);
    }
}
