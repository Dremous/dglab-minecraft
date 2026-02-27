package com.dglab.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class DGLabCommands {
    
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(DGLabCommands::registerCommands);
    }
    
    private static void registerCommands(CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("dglab")
            .then(ClientCommandManager.literal("connect")
                .executes(DGLabCommands::connect)
                .then(ClientCommandManager.argument("server", com.mojang.brigadier.arguments.StringArgumentType.string())
                    .executes(DGLabCommands::connectWithServer)))
            .then(ClientCommandManager.literal("disconnect")
                .executes(DGLabCommands::disconnect))
            .then(ClientCommandManager.literal("bind")
                .then(ClientCommandManager.argument("targetId", com.mojang.brigadier.arguments.StringArgumentType.string())
                    .executes(DGLabCommands::bind)))
            .then(ClientCommandManager.literal("enable")
                .executes(DGLabCommands::enable))
            .then(ClientCommandManager.literal("disable")
                .executes(DGLabCommands::disable))
            .then(ClientCommandManager.literal("channel")
                .then(ClientCommandManager.argument("channel", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 2))
                    .executes(DGLabCommands::setChannel)))
            .then(ClientCommandManager.literal("strength")
                .then(ClientCommandManager.argument("base", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 100))
                    .then(ClientCommandManager.argument("max", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 100))
                        .executes(DGLabCommands::setStrength))))
            .then(ClientCommandManager.literal("test")
                .then(ClientCommandManager.argument("strength", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 100))
                    .executes(DGLabCommands::testPulse)))
            .then(ClientCommandManager.literal("status")
                .executes(DGLabCommands::status))
            .then(ClientCommandManager.literal("qr")
                .executes(DGLabCommands::showQR))
        );
    }
    
    private static int connect(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        DGLabMinecraft.wsClient.connect(DGLabConfig.DEFAULT_SERVER);
        context.getSource().getPlayer().sendMessage(Text.literal("Connecting to DG-LAB server..."), false);
        return 1;
    }
    
    private static int connectWithServer(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        String server = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "server");
        DGLabMinecraft.wsClient.connect(server);
        context.getSource().getPlayer().sendMessage(Text.literal("Connecting to " + server + "..."), false);
        return 1;
    }
    
    private static int disconnect(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        DGLabMinecraft.wsClient.disconnect();
        context.getSource().getPlayer().sendMessage(Text.literal("Disconnected from DG-LAB"), false);
        return 1;
    }
    
    private static int bind(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        String targetId = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "targetId");
        DGLabMinecraft.wsClient.bindToApp(targetId);
        context.getSource().getPlayer().sendMessage(Text.literal("Binding to APP: " + targetId), false);
        return 1;
    }
    
    private static int enable(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        DGLabConfig.enabled.set(true);
        context.getSource().getPlayer().sendMessage(Text.literal("DG-LAB damage pulse enabled"), false);
        return 1;
    }
    
    private static int disable(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        DGLabConfig.enabled.set(false);
        context.getSource().getPlayer().sendMessage(Text.literal("DG-LAB damage pulse disabled"), false);
        return 1;
    }
    
    private static int setChannel(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        int channel = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "channel");
        DGLabConfig.channel.set(channel);
        context.getSource().getPlayer().sendMessage(Text.literal("Channel set to " + channel), false);
        return 1;
    }
    
    private static int setStrength(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        int base = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "base");
        int max = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "max");
        DGLabConfig.baseStrength = base;
        DGLabConfig.maxStrength = max;
        context.getSource().getPlayer().sendMessage(Text.literal("Strength set: base=" + base + ", max=" + max), false);
        return 1;
    }
    
    private static int testPulse(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        int strength = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "strength");
        DGLabMinecraft.wsClient.clearPulseQueue(DGLabConfig.channel.get());
        DGLabMinecraft.wsClient.sendPulse(DGLabConfig.channel.get(), strength, 50, 300);
        context.getSource().getPlayer().sendMessage(Text.literal("Test pulse sent: strength=" + strength), false);
        return 1;
    }
    
    private static int status(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();
        player.sendMessage(Text.literal("=== DG-LAB Status ==="), false);
        player.sendMessage(Text.literal("Connected: " + DGLabMinecraft.wsClient.isConnected()), false);
        player.sendMessage(Text.literal("Bound: " + DGLabMinecraft.wsClient.isBound()), false);
        player.sendMessage(Text.literal("Enabled: " + DGLabConfig.enabled.get()), false);
        player.sendMessage(Text.literal("Channel: " + DGLabConfig.channel.get()), false);
        player.sendMessage(Text.literal("Base Strength: " + DGLabConfig.baseStrength), false);
        player.sendMessage(Text.literal("Max Strength: " + DGLabConfig.maxStrength), false);
        if (DGLabMinecraft.wsClient.getClientId() != null) {
            player.sendMessage(Text.literal("Client ID: " + DGLabMinecraft.wsClient.getClientId()), false);
        }
        return 1;
    }
    
    private static int showQR(CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context) {
        String clientId = DGLabMinecraft.wsClient.getClientId();
        if (clientId == null) {
            context.getSource().getPlayer().sendMessage(Text.literal("Not connected. Use /dglab connect first."), false);
            return 0;
        }
        
        String qrContent = "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#wss://ws.dungeon-lab.cn/" + clientId;
        context.getSource().getPlayer().sendMessage(Text.literal("QR Code content:"), false);
        context.getSource().getPlayer().sendMessage(Text.literal(qrContent), false);
        context.getSource().getPlayer().sendMessage(Text.literal("Scan this with DG-LAB APP to bind"), false);
        return 1;
    }
}
