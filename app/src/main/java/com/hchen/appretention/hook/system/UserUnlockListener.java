package com.hchen.appretention.hook.system;
import static com.hchen.appretention.data.method.SystemMethod.performReceive;
import static com.hchen.appretention.data.path.SystemClass.UserController$3;
import static com.hchen.appretention.data.prop.SystemProp.FALSE;
import static com.hchen.appretention.data.prop.SystemProp.TRUE;
import static com.hchen.appretention.log.SaveLog.USER_UNLOCKED_COMPLETED_PROP;
import android.content.Intent;
import android.os.Bundle;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.utils.SystemPropTool;
@Deprecated
public class UserUnlockListener extends HCBase {
    @Override
    public void init() {
        SystemPropTool.setProp(USER_UNLOCKED_COMPLETED_PROP, FALSE);
        hookMethod(UserController$3,
            performReceive,
            Intent.class, int.class, String.class, Bundle.class, boolean.class, boolean.class, int.class,
            new IHook() {
                @Override
                public void after() {
                    SystemPropTool.setProp(USER_UNLOCKED_COMPLETED_PROP, TRUE);
                    AndroidLog.logI(TAG, "user unlocked completed!!!!");
                }
            }
        );
    }
}
