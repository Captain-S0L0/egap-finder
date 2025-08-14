package com.terriblefriends.gapplefinder.mixin;

import baritone.api.BaritoneAPI;
import com.terriblefriends.gapplefinder.GappleFinderClient;
import com.terriblefriends.gapplefinder.IntegratedServerManager;
import com.terriblefriends.gapplefinder.Messager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    private ChatScreen cs = (ChatScreen) (Object) this;

    @Redirect(
            method = {"keyPressed"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;Z)V"
            )
    )
    private void interceptChatMessage(ChatScreen instance, String string, boolean addToHistory) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(string);
        String[] words = string.split(" ");
        if (words[0].equals(".gapple")) {
            if (words.length == 1) {
                printHelp();
                return;
            }
            switch (words[1]) {
                case "start":
                    if (!GappleFinderClient.client.isInSingleplayer()) {
                        if (!IntegratedServerManager.integratedServerRunning) {
                            IntegratedServerManager.startServer();
                            GappleFinderClient.updatePos = true;
                            GappleFinderClient.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
                            GappleFinderClient.baritoneTargetChestPos = null;
                            GappleFinderClient.finderEnabled = true;
                            Messager.rawchat("Gapple: finder started!");
                            return;
                        }
                    }
                    else if (!GappleFinderClient.finderEnabled) {
                        GappleFinderClient.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
                        GappleFinderClient.baritoneTargetChestPos = null;
                        GappleFinderClient.finderEnabled = true;
                        Messager.rawchat("Gapple: finder started!");
                        return;
                    }
                    Messager.rawchat("Gapple: error! finder already running!");
                    return;
                case "stop":
                    if (GappleFinderClient.finderEnabled) {
                        if (IntegratedServerManager.integratedServerRunning) {
                            GappleFinderClient.updatePos = false;
                            IntegratedServerManager.server.stop(false);
                            IntegratedServerManager.integratedServerRunning = false;
                            GappleFinderClient.baritone.getPathingBehavior().cancelEverything();
                        }
                        Messager.rawchat("Gapple: finder stopped!");
                        GappleFinderClient.finderEnabled = false;
                        return;
                    }
                    Messager.rawchat("Gapple: error! finder already stopped!");
                    return;
                    /*if (GappleFinderClient.updatePos) {
                        GappleFinderClient.updatePos = false;
                        IntegratedServerManager.server.stop(false);
                        IntegratedServerManager.integratedServerRunning = false;
                        Messager.rawchat("Gapple: finder stopped!");
                        GappleFinderClient.baritone.getPathingBehavior().cancelEverything();
                    }
                    else {
                        Messager.rawchat("Gapple: error! finder not active!");
                    }
                    break;*/
                case "auto":
                    if (GappleFinderClient.enableBaritone) {
                        GappleFinderClient.enableBaritone = false;
                        GappleFinderClient.baritoneTargetChestPos = null;
                        Messager.rawchat("Gapple: baritone toggled off!");
                        GappleFinderClient.baritone.getPathingBehavior().cancelEverything();
                    } else {
                        GappleFinderClient.enableBaritone = true;
                        Messager.rawchat("Gapple: baritone toggled on!");
                    }
                    return;
                case "chests":
                    for (BlockPos pos : GappleFinderClient.chestList) {
                        Messager.rawchat(pos.toShortString());
                    }
                case "help":
                    printHelp();
                    return;
                default:
                    Messager.rawchat("Gapple: unknown command! see .gapple help!");
            }
        }
        else {
            cs.sendMessage(string, true);
        }

    }

    private void printHelp() {
        Messager.rawchat("<Gapple Finder 2 by Captain_S0L0>");
        Messager.rawchat("");
        Messager.rawchat("Note: To use on a server, you must create a singleplayer world named \"gapple\" with the seed and datapacks of the server.");
        Messager.rawchat("If you do not know / cannot get the seed and datapacks of the server, then you're out of luck, kiddo.");
        Messager.rawchat("");
        Messager.rawchat("Commands:");
        Messager.rawchat(".gapple start : enable finder");
        Messager.rawchat(".gapple stop : disable finder");
        Messager.rawchat(".gapple auto : toggle baritone automation");
    }
}
