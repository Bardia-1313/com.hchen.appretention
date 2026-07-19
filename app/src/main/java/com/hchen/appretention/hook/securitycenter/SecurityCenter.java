package com.hchen.appretention.hook.securitycenter;
import com.hchen.appretention.hook.xiaomi.OemProcessProtection;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
@HookEntrance(targetPackage = "com.miui.securitycenter", targetBrand = "Xiaomi")
public class SecurityCenter extends HCBase {
    @Override
    public void init() {
        OemProcessProtection.installSecurityCenter();
    }
}
