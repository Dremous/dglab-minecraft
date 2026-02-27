package com.dglab.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class DGLabLocalServer extends WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("dglab-minecraft");
    private static final Gson GSON = new Gson();
    
    private volatile boolean isRunning = false;
    private volatile boolean isConnected = false;
    private WebSocket client;
    private Timer heartbeatTimer;
    
    private String clientId = "1234-123456789-12345-12345-01";
    private String targetId = "";
    private int aStrength = 0;
    private int bStrength = 0;
    private int aMaxStrength = 200;
    private int bMaxStrength = 200;
    
    public DGLabLocalServer(int port) {
        super(new InetSocketAddress(port));
    }
    
    public DGLabLocalServer(InetSocketAddress address) {
        super(address);
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (!isConnected) {
            isConnected = true;
            client = conn;
            
            JsonObject bindMsg = new JsonObject();
            bindMsg.addProperty("type", "bind");
            bindMsg.addProperty("clientId", targetId.isEmpty() ? "1234-123456789-12345-12345-00" : targetId);
            bindMsg.addProperty("targetId", "");
            bindMsg.addProperty("message", "targetId");
            conn.send(GSON.toJson(bindMsg));
            
            LOGGER.info("DG-LAB APP connected");
            
            startHeartbeat();
        } else {
            JsonObject errorMsg = new JsonObject();
            errorMsg.addProperty("type", "error");
            errorMsg.addProperty("message", "400");
            conn.send(GSON.toJson(errorMsg));
        }
    }
    
    private void startHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
        }
        heartbeatTimer = new Timer(true);
        heartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isConnected && client != null) {
                    JsonObject heartbeat = new JsonObject();
                    heartbeat.addProperty("type", "heartbeat");
                    heartbeat.addProperty("clientId", targetId.isEmpty() ? "1234-123456789-12345-12345-00" : targetId);
                    heartbeat.addProperty("targetId", clientId);
                    heartbeat.addProperty("message", "200");
                    client.send(GSON.toJson(heartbeat));
                }
            }
        }, 0, 60000);
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (client != null && conn.equals(client)) {
            isConnected = false;
            client = null;
            targetId = "";
            stopHeartbeat();
            LOGGER.info("DG-LAB APP disconnected");
        }
    }
    
    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JsonObject json = GSON.fromJson(message, JsonObject.class);
            String type = json.has("type") ? json.get("type").getAsString() : "";
            String msgClientId = json.has("clientId") ? json.get("clientId").getAsString() : "";
            String msgTargetId = json.has("targetId") ? json.get("targetId").getAsString() : "";
            String msgContent = json.has("message") ? json.get("message").getAsString() : "";
            
            if ("bind".equals(type) && "DGLAB".equals(msgContent) && clientId.equals(msgClientId)) {
                targetId = msgTargetId;
                
                JsonObject response = new JsonObject();
                response.addProperty("type", "bind");
                response.addProperty("clientId", targetId);
                response.addProperty("targetId", clientId);
                response.addProperty("message", "200");
                conn.send(GSON.toJson(response));
                
                LOGGER.info("Bound to APP: {}", targetId);
            } else if ("msg".equals(type)) {
                handleMessage(msgContent);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle message", e);
        }
    }
    
    private void handleMessage(String message) {
        if (message.startsWith("strength-")) {
            String[] parts = message.substring(9).split("\\+");
            if (parts.length >= 4) {
                try {
                    aStrength = Integer.parseInt(parts[0]);
                    bStrength = Integer.parseInt(parts[1]);
                    aMaxStrength = Integer.parseInt(parts[2]);
                    bMaxStrength = Integer.parseInt(parts[3]);
                    LOGGER.debug("Strength update: A={}, B={}, MaxA={}, MaxB={}", aStrength, bStrength, aMaxStrength, bMaxStrength);
                } catch (NumberFormatException e) {
                    LOGGER.error("Failed to parse strength", e);
                }
            }
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error("WebSocket server error", ex);
    }
    
    @Override
    public void onStart() {
        isRunning = true;
        LOGGER.info("DG-LAB local server started on port {}", getPort());
    }
    
    public void sendStrength(int channel, int mode, int value) {
        if (!isConnected || client == null) return;
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "msg");
        msg.addProperty("clientId", targetId);
        msg.addProperty("targetId", clientId);
        msg.addProperty("message", PulseGenerator.buildStrengthMessage(channel, mode, value));
        client.send(GSON.toJson(msg));
    }
    
    public void sendPulse(int channel, String[] pulseData) {
        if (!isConnected || client == null) return;
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "msg");
        msg.addProperty("clientId", targetId);
        msg.addProperty("targetId", clientId);
        
        String pulseArray = PulseGenerator.buildPulseArrayJson(pulseData);
        String channelStr = PulseGenerator.getChannelName(channel);
        msg.addProperty("message", "pulse-" + channelStr + ":" + pulseArray);
        client.send(GSON.toJson(msg));
    }
    
    public void clearPulseQueue(int channel) {
        if (!isConnected || client == null) return;
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "msg");
        msg.addProperty("clientId", targetId);
        msg.addProperty("targetId", clientId);
        msg.addProperty("message", "clear-" + channel);
        client.send(GSON.toJson(msg));
    }
    
    public void sendDamagePulse(float damage, DamageType damageType) {
        if (!isConnected || !DGLabConfig.enabled.get()) return;
        
        clearPulseQueue(DGLabConfig.channel.get());
        
        String[] pulseData = PulseGenerator.generateDamagePulse(damage, damageType);
        sendPulse(DGLabConfig.channel.get(), pulseData);
        
        LOGGER.info("Sent damage pulse: damage={}, type={}", damage, damageType.name());
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isBound() {
        return isConnected && !targetId.isEmpty();
    }
    
    public int getPort() {
        return getAddress().getPort();
    }
    
    public String getQrContent(String ipAddress) {
        return "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#ws://" + ipAddress + ":" + getPort() + "/" + clientId;
    }
    
    public int getAStrength() {
        return aStrength;
    }
    
    public int getBStrength() {
        return bStrength;
    }
    
    public int getAMaxStrength() {
        return aMaxStrength;
    }
    
    public int getBMaxStrength() {
        return bMaxStrength;
    }
}
