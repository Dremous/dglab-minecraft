package com.dglab.minecraft;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DGLabConfig {
    public static final String DEFAULT_SERVER = "wss://ws.dungeon-lab.cn";
    public static final int DEFAULT_PORT = 9999;
    
    public static final AtomicInteger channel = new AtomicInteger(1);
    public static int baseStrength = 20;
    public static int maxStrength = 80;
    public static int baseDuration = 100;
    public static int maxDuration = 500;
    public static int baseFrequency = 50;
    public static final AtomicBoolean enabled = new AtomicBoolean(true);
    public static boolean autoReconnect = true;
    public static float damageMultiplier = 1.0f;
    
    public static boolean useLocalServer = true;
    public static int serverPort = DEFAULT_PORT;
    public static boolean autoStartServer = true;
    public static String currentNetworkName = "";
    
    public static int calculateStrength(float damage) {
        int calculated = (int) (baseStrength + (damage * 10 * damageMultiplier));
        return Math.min(maxStrength, Math.max(baseStrength, calculated));
    }
    
    public static int calculateDuration(float damage) {
        int calculated = (int) (baseDuration + (damage * 30 * damageMultiplier));
        return Math.min(maxDuration, Math.max(baseDuration, calculated));
    }
    
    public static int calculateFrequency(float damage) {
        return (int) (baseFrequency + Math.min(damage * 5, 50));
    }
}
