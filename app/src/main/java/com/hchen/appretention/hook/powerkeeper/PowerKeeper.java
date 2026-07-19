package com.hchen.appretention.hook.powerkeeper;
import com.hchen.appretention.hook.xiaomi.OemProcessProtection;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
@HookEntrance(targetPackage = "com.miui.powerkeeper", targetBrand = "Xiaomi")
public class PowerKeeper extends HCBase {
    @Override
    public void init() {
        OemProcessProtection.installPowerKeeper();
    }
}
