package com.dglab.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

public class DGLabWebSocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("dglab-minecraft");
    private static final Gson GSON = new Gson();
    
    private WebSocketClient client;
    private String clientId;
    private String targetId;
    private volatile boolean connected = false;
    private volatile boolean bound = false;
    private String lastServerUrl;
    
    public void connect(String serverUrl) {
        if (connected) {
            LOGGER.warn("Already connected");
            return;
        }
        
        this.lastServerUrl = serverUrl;
        
        try {
            clientId = UUID.randomUUID().toString();
            URI uri = new URI(serverUrl + "/" + clientId);
            
            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    LOGGER.info("Connected to DG-LAB server");
                    connected = true;
                }
                
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LOGGER.info("Disconnected from DG-LAB server: {}", reason);
                    connected = false;
                    bound = false;
                    scheduleReconnect();
                }
                
                private void scheduleReconnect() {
                    if (DGLabConfig.autoReconnect && DGLabConfig.enabled.get()) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(5000);
                                if (!connected) {
                                    LOGGER.info("Attempting to reconnect...");
                                    DGLabWebSocketClient.this.connect(lastServerUrl);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    LOGGER.error("WebSocket error", ex);
                    connected = false;
                    bound = false;
                }
            };
            
            client.connect();
        } catch (Exception e) {
            LOGGER.error("Failed to connect", e);
        }
    }
    
    public void disconnect() {
        if (client != null) {
            client.close();
            connected = false;
            bound = false;
        }
    }
    
    public void bindToApp(String appTargetId) {
        if (!connected) {
            LOGGER.warn("Not connected to server");
            return;
        }
        
        this.targetId = appTargetId;
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "bind");
        msg.addProperty("clientId", clientId);
        msg.addProperty("targetId", targetId);
        msg.addProperty("message", "DGLAB");
        
        client.send(GSON.toJson(msg));
    }
    
    private void handleMessage(String message) {
        try {
            JsonObject json = GSON.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();
            
            if ("bind".equals(type)) {
                if (json.has("message") && "targetId".equals(json.get("message").getAsString())) {
                    String returnedClientId = json.get("clientId").getAsString();
                    LOGGER.info("Received client ID: {}", returnedClientId);
                } else if (json.has("message") && "200".equals(json.get("message").getAsString())) {
                    LOGGER.info("Successfully bound to APP");
                    bound = true;
                }
            } else if ("msg".equals(type)) {
                String msgContent = json.get("message").getAsString();
                if (msgContent.startsWith("strength-")) {
                    LOGGER.info("Received strength update: {}", msgContent);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle message", e);
        }
    }
    
    public void sendPulseData(int channel, String[] pulseData) {
        if (!connected || !bound) {
            LOGGER.warn("Not connected or not bound");
            return;
        }
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "msg");
        msg.addProperty("clientId", clientId);
        msg.addProperty("targetId", targetId);
        
        String pulseArray = PulseGenerator.buildPulseArrayJson(pulseData);
        String channelStr = PulseGenerator.getChannelName(channel);
        msg.addProperty("message", "pulse-" + channelStr + ":" + pulseArray);
        
        client.send(GSON.toJson(msg));
        LOGGER.info("Sent pulse data to channel {}", channel);
    }
    
    public void sendPulse(int channel, int strength, int frequency, int duration) {
        String[] pulseData = PulseGenerator.generatePulse(channel, strength, frequency, duration);
        sendPulseData(channel, pulseData);
    }
    
    public void setStrength(int channel, int mode, int value) {
        if (!connected || !bound) {
            LOGGER.warn("Not connected or not bound");
            return;
        }
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "msg");
        msg.addProperty("clientId", clientId);
        msg.addProperty("targetId", targetId);
        msg.addProperty("message", PulseGenerator.buildStrengthMessage(channel, mode, value));
        
        client.send(GSON.toJson(msg));
    }
    
    public void clearPulseQueue(int channel) {
        if (!connected || !bound) {
            return;
        }
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "msg");
        msg.addProperty("clientId", clientId);
        msg.addProperty("targetId", targetId);
        msg.addProperty("message", "clear-" + channel);
        
        client.send(GSON.toJson(msg));
    }
    
    public void sendDamagePulse(float damage) {
        sendDamagePulse(damage, DamageType.GENERIC);
    }
    
    public void sendDamagePulse(float damage, DamageType damageType) {
        if (!connected || !bound || !DGLabConfig.enabled.get()) {
            return;
        }
        
        clearPulseQueue(DGLabConfig.channel.get());
        
        String[] pulseData = PulseGenerator.generateDamagePulse(damage, damageType);
        sendPulseData(DGLabConfig.channel.get(), pulseData);
        
        LOGGER.info("Sent damage pulse: damage={}, type={}, waveType={}", 
            damage, damageType.name(), damageType.waveType.name());
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean isBound() {
        return bound;
    }
    
    public String getClientId() {
        return clientId;
    }
}
