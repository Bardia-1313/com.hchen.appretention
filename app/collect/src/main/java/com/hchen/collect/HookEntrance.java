package com.hchen.collect;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface HookEntrance {
    String targetBrand() default "Any";
    String targetPackage();
    int[] targetSdks() default 0;
    float targetOS() default -1f;
    boolean isHyperOS() default false;
    boolean downward() default false;
    boolean upward() default false;
}
