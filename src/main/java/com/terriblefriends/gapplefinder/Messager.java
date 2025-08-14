package com.terriblefriends.gapplefinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;

public class Messager {
    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private static final MessageType messageType = DynamicRegistryManager.BUILTIN.get().get(Registry.MESSAGE_TYPE_KEY).get(new Identifier("system"));

    public static void actionBar(String message){
        Text text = Text.literal(message);
        minecraftClient.inGameHud.setOverlayMessage(text,false);
    }
    public static void rawactionBar(String message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Text text = Text.literal(message);
        minecraftClient.inGameHud.setOverlayMessage(text,false);
    }

    public static void chat(String message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Text text = Text.literal(message);
        minecraftClient.inGameHud.onGameMessage(messageType,text);
    }

    public static void rawchat(String message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Text text =Text.literal(message);
        minecraftClient.inGameHud.onGameMessage(messageType,text);
    }

    public static void chatText(Text message){
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.inGameHud.onGameMessage(messageType,message);
    }
}

