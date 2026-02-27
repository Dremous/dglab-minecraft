package com.dglab.minecraft;

public final class DGLabConstants {
    private DGLabConstants() {}
    
    public static final String MOD_ID = "dglab-minecraft";
    public static final String MOD_NAME = "DG-LAB Minecraft Integration";
    
    public static final String DEFAULT_SERVER = "wss://ws.dungeon-lab.cn";
    public static final int DEFAULT_PORT = 9999;
    
    public static final int HEARTBEAT_INTERVAL_MS = 60000;
    public static final int DAMAGE_COOLDOWN_MS = 50;
    public static final int QR_UPDATE_INTERVAL = 10;
    public static final int RECONNECT_DELAY_MS = 5000;
    
    public static final int DEFAULT_CHANNEL = 1;
    public static final int DEFAULT_BASE_STRENGTH = 20;
    public static final int DEFAULT_MAX_STRENGTH = 80;
    public static final int DEFAULT_BASE_DURATION = 100;
    public static final int DEFAULT_MAX_DURATION = 500;
    public static final int DEFAULT_BASE_FREQUENCY = 50;
    
    public static final float DEFAULT_DAMAGE_MULTIPLIER = 1.0f;
    
    public static final int QR_CODE_SIZE = 150;
    public static final int GUI_PANEL_WIDTH = 200;
    public static final int GUI_PADDING = 10;
    
    public static final String KEY_OPEN_GUI = "key.dglab.open_gui";
    public static final String KEY_CATEGORY = "category.dglab";
}
