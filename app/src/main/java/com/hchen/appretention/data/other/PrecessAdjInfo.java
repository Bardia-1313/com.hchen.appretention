package com.hchen.appretention.data.other;
public final class PrecessAdjInfo {
    public static final int INVALID_ADJ = -10000;
    public static final int UNKNOWN_ADJ = 1001;
    public static final int CACHED_APP_MAX_ADJ = 999;
    public static final int CACHED_APP_MIN_ADJ = 900;
    public static final int CACHED_APP_LMK_FIRST_ADJ = 950;
    public static final int CACHED_APP_IMPORTANCE_LEVELS = 5;
    public static final int SERVICE_B_ADJ = 800;
    public static final int PREVIOUS_APP_ADJ = 700;
    public static final int HOME_APP_ADJ = 600;
    public static final int SERVICE_ADJ = 500;
    public static final int HEAVY_WEIGHT_APP_ADJ = 400;
    public static final int BACKUP_APP_ADJ = 300;
    public static final int PERCEPTIBLE_LOW_APP_ADJ = 250;
    public static final int PERCEPTIBLE_MEDIUM_APP_ADJ = 225;
    public static final int PERCEPTIBLE_APP_ADJ = 200;
    public static final int VISIBLE_APP_ADJ = 100;
    public static final int VISIBLE_APP_LAYER_MAX = PERCEPTIBLE_APP_ADJ - VISIBLE_APP_ADJ - 1;
    public static final int PERCEPTIBLE_RECENT_FOREGROUND_APP_ADJ = 50;
    public static final int FOREGROUND_APP_ADJ = 0;
    public static final int PERSISTENT_SERVICE_ADJ = -700;
    public static final int PERSISTENT_PROC_ADJ = -800;
    public static final int SYSTEM_ADJ = -900;
    public static final int NATIVE_ADJ = -1000;
}
