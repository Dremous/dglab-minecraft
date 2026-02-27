package com.dglab.minecraft.gui;

import com.dglab.minecraft.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;

public class DGLabScreen extends Screen {
    private static final int QR_SIZE = 150;
    private static final int PANEL_WIDTH = 200;
    private static final int PADDING = 10;
    private static final int QR_UPDATE_INTERVAL = 10;
    
    private BufferedImage qrImage;
    private int tickCounter = 0;
    private boolean needsQRUpdate = true;
    private SliderWidget baseStrengthSlider;
    private SliderWidget maxStrengthSlider;
    private ButtonWidget modeButton;
    private ButtonWidget serverButton;
    private ButtonWidget networkButton;
    
    public DGLabScreen() {
        super(Text.translatable("gui.dglab.title"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        int leftPanelX = PADDING + 10;
        int startY = 45;
        
        modeButton = ButtonWidget.builder(
            DGLabConfig.useLocalServer 
                ? Text.translatable("gui.dglab.mode_local")
                : Text.translatable("gui.dglab.mode_remote"),
            button -> {
                DGLabConfig.useLocalServer = !DGLabConfig.useLocalServer;
                button.setMessage(DGLabConfig.useLocalServer 
                    ? Text.translatable("gui.dglab.mode_local")
                    : Text.translatable("gui.dglab.mode_remote"));
                requestQRUpdate();
            }
        ).dimensions(leftPanelX, startY, PANEL_WIDTH, 20).build();
        this.addDrawableChild(modeButton);
        
        if (DGLabConfig.useLocalServer) {
            initLocalServerMode(leftPanelX, startY + 25);
        } else {
            initRemoteServerMode(leftPanelX, startY + 25);
        }
        
        updateQRCode();
    }
    
    private void initLocalServerMode(int x, int startY) {
        serverButton = ButtonWidget.builder(
            DGLabMinecraft.localServer.isRunning()
                ? Text.translatable("gui.dglab.server_stop")
                : Text.translatable("gui.dglab.server_start"),
            button -> {
                if (DGLabMinecraft.localServer.isRunning()) {
                    DGLabMinecraft.stopLocalServer();
                    button.setMessage(Text.translatable("gui.dglab.server_start"));
                } else {
                    DGLabMinecraft.startLocalServer();
                    button.setMessage(Text.translatable("gui.dglab.server_stop"));
                }
                requestQRUpdate();
            }
        ).dimensions(x, startY, PANEL_WIDTH, 20).build();
        this.addDrawableChild(serverButton);
        
        String currentNetwork = DGLabMinecraft.networkAdapter.getCurrentNetworkName();
        String currentIp = DGLabMinecraft.networkAdapter.getCurrentIpAddress();
        networkButton = ButtonWidget.builder(
            Text.literal(currentNetwork + ": " + currentIp),
            button -> {
                String nextIp = DGLabMinecraft.networkAdapter.getNextIpAddress();
                String nextName = DGLabMinecraft.networkAdapter.getCurrentNetworkName();
                button.setMessage(Text.literal(nextName + ": " + nextIp));
                DGLabConfig.currentNetworkName = nextName;
                requestQRUpdate();
            }
        ).dimensions(x, startY + 25, PANEL_WIDTH, 20).build();
        this.addDrawableChild(networkButton);
        
        addSettingsWidgets(x, startY + 55);
    }
    
    private void initRemoteServerMode(int x, int startY) {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.dglab.connect"), button -> {
            DGLabMinecraft.wsClient.connect(DGLabConfig.DEFAULT_SERVER);
            requestQRUpdate();
        }).dimensions(x, startY, 95, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.dglab.disconnect"), button -> {
            DGLabMinecraft.wsClient.disconnect();
            requestQRUpdate();
        }).dimensions(x + 105, startY, 95, 20).build());
        
        addSettingsWidgets(x, startY + 30);
    }
    
    private void addSettingsWidgets(int x, int startY) {
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.dglab.channel").copy().append(": " + DGLabConfig.channel.get()), 
            button -> {
                DGLabConfig.channel.set(DGLabConfig.channel.get() == 1 ? 2 : 1);
                button.setMessage(Text.translatable("gui.dglab.channel").copy().append(": " + DGLabConfig.channel.get()));
            }
        ).dimensions(x, startY, PANEL_WIDTH, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            DGLabConfig.enabled.get() 
                ? Text.translatable("gui.dglab.enabled").copy().append(": ON")
                : Text.translatable("gui.dglab.enabled").copy().append(": OFF"),
            button -> {
                DGLabConfig.enabled.set(!DGLabConfig.enabled.get());
                button.setMessage(DGLabConfig.enabled.get() 
                    ? Text.translatable("gui.dglab.enabled").copy().append(": ON")
                    : Text.translatable("gui.dglab.enabled").copy().append(": OFF"));
            }
        ).dimensions(x, startY + 25, PANEL_WIDTH, 20).build());
        
        baseStrengthSlider = new SliderWidget(x, startY + 50, PANEL_WIDTH, 20, 
            Text.translatable("gui.dglab.base_strength").copy().append(": " + DGLabConfig.baseStrength), 
            DGLabConfig.baseStrength / 100.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.translatable("gui.dglab.base_strength").copy().append(": " + (int)(this.value * 100)));
            }
            
            @Override
            protected void applyValue() {
                DGLabConfig.baseStrength = (int)(this.value * 100);
            }
        };
        this.addDrawableChild(baseStrengthSlider);
        
        maxStrengthSlider = new SliderWidget(x, startY + 75, PANEL_WIDTH, 20, 
            Text.translatable("gui.dglab.max_strength").copy().append(": " + DGLabConfig.maxStrength), 
            DGLabConfig.maxStrength / 100.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.translatable("gui.dglab.max_strength").copy().append(": " + (int)(this.value * 100)));
            }
            
            @Override
            protected void applyValue() {
                DGLabConfig.maxStrength = (int)(this.value * 100);
            }
        };
        this.addDrawableChild(maxStrengthSlider);
        
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.dglab.test_pulse"), button -> {
            if (DGLabConfig.useLocalServer && DGLabMinecraft.localServer.isBound()) {
                DGLabMinecraft.localServer.clearPulseQueue(DGLabConfig.channel.get());
                DGLabMinecraft.localServer.sendPulse(DGLabConfig.channel.get(), 
                    PulseGenerator.generatePulse(DGLabConfig.channel.get(), DGLabConfig.baseStrength, 50, 300));
            } else if (!DGLabConfig.useLocalServer && DGLabMinecraft.wsClient.isBound()) {
                DGLabMinecraft.wsClient.clearPulseQueue(DGLabConfig.channel.get());
                DGLabMinecraft.wsClient.sendPulse(DGLabConfig.channel.get(), DGLabConfig.baseStrength, 50, 300);
            }
        }).dimensions(x, startY + 100, PANEL_WIDTH, 20).build());
    }
    
    private void updateQRCode() {
        qrImage = null;
        
        if (DGLabConfig.useLocalServer) {
            if (DGLabMinecraft.localServer.isRunning()) {
                String ipAddress = DGLabMinecraft.networkAdapter.getCurrentIpAddress();
                String qrContent = DGLabMinecraft.localServer.getQrContent(ipAddress);
                qrImage = QRCodeGenerator.generateQRCode(qrContent, QR_SIZE, QR_SIZE);
            }
        } else {
            String clientId = DGLabMinecraft.wsClient.getClientId();
            if (clientId != null && DGLabMinecraft.wsClient.isConnected()) {
                String qrContent = "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#wss://ws.dungeon-lab.cn/" + clientId;
                qrImage = QRCodeGenerator.generateQRCode(qrContent, QR_SIZE, QR_SIZE);
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        tickCounter++;
        if (needsQRUpdate && tickCounter >= QR_UPDATE_INTERVAL) {
            updateQRCode();
            tickCounter = 0;
            needsQRUpdate = false;
        }
    }
    
    public void requestQRUpdate() {
        needsQRUpdate = true;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        Text status;
        int statusColor;
        
        if (DGLabConfig.useLocalServer) {
            if (DGLabMinecraft.localServer.isBound()) {
                status = Text.translatable("gui.dglab.status.connected_bound");
                statusColor = 0x55FF55;
            } else if (DGLabMinecraft.localServer.isRunning()) {
                status = Text.translatable("gui.dglab.status.waiting");
                statusColor = 0xFFFF55;
            } else {
                status = Text.translatable("gui.dglab.status.server_stopped");
                statusColor = 0xFF5555;
            }
        } else {
            if (DGLabMinecraft.wsClient.isBound()) {
                status = Text.translatable("gui.dglab.status.connected_bound");
                statusColor = 0x55FF55;
            } else if (DGLabMinecraft.wsClient.isConnected()) {
                status = Text.translatable("gui.dglab.status.connected");
                statusColor = 0xFFFF55;
            } else {
                status = Text.translatable("gui.dglab.status.disconnected");
                statusColor = 0xFF5555;
            }
        }
        
        int leftPanelX = PADDING + 10;
        context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.dglab.title"), leftPanelX, 15, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, status, leftPanelX, 30, statusColor);
        
        int qrAreaX = this.width - QR_SIZE - PADDING * 3;
        int qrAreaY = PADDING + 20;
        int qrAreaWidth = QR_SIZE + PADDING * 4;
        int qrAreaHeight = QR_SIZE + PADDING * 4 + 30;
        
        context.fill(qrAreaX - PADDING, qrAreaY - PADDING, 
            qrAreaX + QR_SIZE + PADDING * 3, qrAreaY + QR_SIZE + PADDING * 3 + 20, 
            0x80000000);
        
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("gui.dglab.qr_title"), 
            qrAreaX + QR_SIZE / 2 + PADDING, qrAreaY, 0xFFFFFF);
        
        int qrY = qrAreaY + 15;
        
        if (qrImage != null) {
            int qrX = qrAreaX + PADDING;
            
            context.fill(qrX - 5, qrY - 5, qrX + QR_SIZE + 5, qrY + QR_SIZE + 5, 0xFFFFFFFF);
            renderQRImage(context, qrX, qrY);
            
            context.drawCenteredTextWithShadow(this.textRenderer, 
                Text.translatable("gui.dglab.scan_qr"), 
                qrAreaX + QR_SIZE / 2 + PADDING, qrY + QR_SIZE + 10, 0xAAAAAA);
        } else {
            String messageKey = DGLabConfig.useLocalServer ? "gui.dglab.start_server_for_qr" : "gui.dglab.connect_for_qr";
            context.drawCenteredTextWithShadow(this.textRenderer, 
                Text.translatable(messageKey), 
                qrAreaX + QR_SIZE / 2 + PADDING, qrY + QR_SIZE / 2, 0xAAAAAA);
        }
        
        if (DGLabConfig.useLocalServer && DGLabMinecraft.localServer.isRunning()) {
            String ip = DGLabMinecraft.networkAdapter.getCurrentIpAddress();
            int port = DGLabMinecraft.localServer.getPort();
            context.drawCenteredTextWithShadow(this.textRenderer, 
                Text.literal("ws://" + ip + ":" + port), 
                qrAreaX + QR_SIZE / 2 + PADDING, qrY + QR_SIZE + 25, 0x888888);
        }
    }
    
    private void renderQRImage(DrawContext context, int x, int y) {
        if (qrImage == null) return;
        
        int width = qrImage.getWidth();
        int height = qrImage.getHeight();
        
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int rgb = qrImage.getRGB(px, py);
                boolean isBlack = (rgb & 0x00FFFFFF) == 0;
                if (isBlack) {
                    context.fill(x + px, y + py, x + px + 1, y + py + 1, 0xFF000000);
                }
            }
        }
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
