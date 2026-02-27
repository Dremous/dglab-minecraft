package com.dglab.minecraft;

import com.dglab.minecraft.gui.DGLabScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DGLabMinecraft implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("dglab-minecraft");
    
    public static DGLabLocalServer localServer;
    public static DGLabWebSocketClient wsClient;
    public static NetworkAdapter networkAdapter;
    
    public static DamageType lastDamageType = DamageType.GENERIC;
    public static float lastDamageAmount = 0;
    private static volatile long lastDamageTime = 0;
    
    private static KeyBinding openGuiKey;
    
    public static final int DEFAULT_PORT = 9999;
    
    @Override
    public void onInitializeClient() {
        networkAdapter = new NetworkAdapter();
        localServer = new DGLabLocalServer(DGLabConfig.serverPort);
        wsClient = new DGLabWebSocketClient();
        
        DamageListener.register();
        DGLabCommands.register();
        
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dglab.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "category.dglab"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                client.setScreen(new DGLabScreen());
            }
            
            if (lastDamageAmount > 0 && System.currentTimeMillis() - lastDamageTime > 50) {
                DamageType type = lastDamageType;
                float amount = lastDamageAmount;
                lastDamageAmount = 0;
                lastDamageType = DamageType.GENERIC;
                
                if (DGLabConfig.enabled.get()) {
                    if (DGLabConfig.useLocalServer && localServer.isBound()) {
                        localServer.sendDamagePulse(amount, type);
                    } else if (!DGLabConfig.useLocalServer && wsClient.isBound()) {
                        wsClient.sendDamagePulse(amount, type);
                    }
                }
            }
        });
        
        if (DGLabConfig.autoStartServer) {
            startLocalServer();
        }
        
        LOGGER.info("DG-LAB Minecraft Mod initialized");
        LOGGER.info("Press J to open GUI");
    }
    
    public static void startLocalServer() {
        if (localServer != null && !localServer.isRunning()) {
            try {
                localServer.start();
                LOGGER.info("Local server started on port {}", localServer.getPort());
            } catch (Exception e) {
                LOGGER.error("Failed to start local server", e);
            }
        }
    }
    
    public static void stopLocalServer() {
        if (localServer != null && localServer.isRunning()) {
            try {
                localServer.stop();
                LOGGER.info("Local server stopped");
            } catch (Exception e) {
                LOGGER.error("Failed to stop local server", e);
            }
        }
    }
    
    public static void restartLocalServer(int port) {
        stopLocalServer();
        localServer = new DGLabLocalServer(port);
        startLocalServer();
    }
    
    public static void setLastDamage(float amount, DamageType type) {
        lastDamageAmount = amount;
        lastDamageType = type;
        lastDamageTime = System.currentTimeMillis();
    }
}
