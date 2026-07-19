package com.hchen.appretention.hook.samsung;
import static com.hchen.appretention.data.method.HyperMethod.init;
import static com.hchen.appretention.data.method.OneUiMethod.setLmkdParameter;
import static com.hchen.appretention.data.path.OneUiClass.DynamicHiddenApp$LmkdParameter;
import static com.hchen.appretention.data.path.SystemClass.ActiveUids;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.PlatformCompat;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.hooktool.core.CoreTool.callStaticMethod;
import static com.hchen.hooktool.core.CoreTool.findClass;
import static com.hchen.hooktool.core.CoreTool.hookMethod;
import static com.hchen.appretention.log.AppRetentionXposedLog.logD;
import static com.hchen.appretention.log.AppRetentionXposedLog.logI;
import android.util.Pair;
import com.hchen.hooktool.core.CoreTool;
import com.hchen.hooktool.hook.IHook;
import java.util.Arrays;
import java.util.HashMap;
public final class LmkdParameter {
    private static final String TAG = "LmkdParameter";
    final static String[] mLmkdParameter = new String[]{
        "LMK_LOW_ADJ", "LMK_MEDIUM_ADJ", "LMK_CRITICAL_ADJ", "LMK_DEBUG",
        "LMK_CRITICAL_UPGRADE", "LMK_UPGRADE_PRESSURE", "LMK_DOWNGRADE_PRESSURE",
        "LMK_KILL_HEAVIEST_TASK", "LMK_KILL_TIMEOUT_MS", "LMK_USE_MINFREE_LEVELS",
        "LMK_ENABLE_USERSPACE_LMK", "LMK_ENABLE_CMARBINFREE_SUB", "LMK_ENABLE_UPGRADE_CRIADJ",
        "LMK_FREELIMIT_ENABLE", "LMK_FREELIMIT_VAL", "LMK_PSI_LOW_TH", "LMK_PSI_MEDIUM_TH",
        "LMK_PSI_CRITICAL_TH", "LMK_SET_SWAPTOTAL", "LMK_SET_BG_KEEPING"
    };
    final static HashMap<Integer, Pair<String, Integer>> mOrdinalAndParameterMap = new HashMap<>();
    static boolean isInit = false;
    public static void init() {
        if (isInit) return;
        Class<?> lmkdParameter = findClass(DynamicHiddenApp$LmkdParameter);
        if (lmkdParameter == null)
            return;
        Arrays.stream(mLmkdParameter).forEach(s -> {
            Object param = CoreTool.getStaticField(lmkdParameter, s);
            if (param == null) return;
            Integer ordinal = (Integer) CoreTool.callMethod(param, "ordinal");
            if (ordinal == null) return;
            switch (s) {
                case "LMK_LOW_ADJ", "LMK_MEDIUM_ADJ", "LMK_CRITICAL_ADJ":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 1001));
                    break;
                case "LMK_DEBUG", "LMK_CRITICAL_UPGRADE", "LMK_KILL_HEAVIEST_TASK",
                     "LMK_ENABLE_UPGRADE_CRIADJ", "LMK_FREELIMIT_ENABLE":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 0));
                    break;
                case "LMK_UPGRADE_PRESSURE", "LMK_DOWNGRADE_PRESSURE":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 100));
                    break;
                case "LMK_SET_BG_KEEPING":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 1));
                    break;
            }
        });
        mOrdinalAndParameterMap.forEach((integer, stringIntegerPair) ->
            logI(TAG, "Map: lmkd parameter: " + stringIntegerPair.first + ", value: " + stringIntegerPair.second + ", ordinal: " + integer));
        isInit = true;
    }
    public static void forceReplace() {
        hookMethod(ProcessList,
            init,
            ActivityManagerService, ActiveUids, PlatformCompat,
            new IHook() {
                @Override
                public void after() {
                    mOrdinalAndParameterMap.forEach((integer, stringIntegerPair) -> {
                        callStaticMethod(ProcessList, setLmkdParameter, integer, stringIntegerPair.second);
                        logD(TAG, "Force ste lmkd parameter: " + stringIntegerPair.first + ", value: " + stringIntegerPair.second + ", ordinal: " + integer);
                    });
                }
            }
        );
    }
    public static void replace(IHook iHook) {
        if (!isInit)
            init();
        int ordinal = (int) iHook.getArg(0);
        int value = (int) iHook.getArg(1);
        if (mOrdinalAndParameterMap.get(ordinal) != null) {
            Pair<String, Integer> param = mOrdinalAndParameterMap.get(ordinal);
            assert param != null;
            int newValue = param.second;
            iHook.setArg(1, newValue);
            logD(TAG, "Lmkd parameter: " + param.first + ", old value: " + value + ", new value: " + newValue);
        }
    }
}
