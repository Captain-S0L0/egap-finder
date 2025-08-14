package com.terriblefriends.gapplefinder.commands;

import baritone.api.BaritoneAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.terriblefriends.gapplefinder.GappleFinderClient;
import com.terriblefriends.gapplefinder.IntegratedServerManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class GappleCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        try {
            Class.forName("net.fabricmc.fabric.impl.command.client.ClientCommandInternals");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not find ClientCommandInternals, /gapple command not available");
            return;
        }

        fabricClientCommandSourceCommandDispatcher.register(literal("gapple")
                .then(literal("start")
                        .executes(ctx -> start(ctx.getSource())))
                .then(literal("stop")
                        .executes(ctx -> stop(ctx.getSource())))
                .then(literal("auto")
                        .executes(ctx -> auto(ctx.getSource())))
                .then(literal("help")
                        .executes(ctx -> help(ctx.getSource())))
                .then(literal("searchDistance")
                        .then(argument("radius", IntegerArgumentType.integer(0, 32))
                                .executes(ctx -> setSearchDistance(ctx.getSource(),IntegerArgumentType.getInteger(ctx, "radius")))))
        );
    }

    private static int start(FabricClientCommandSource source) throws CommandSyntaxException {
        if (!GappleFinderClient.client.isInSingleplayer()) {
            if (!IntegratedServerManager.integratedServerRunning) {
                IntegratedServerManager.startServer();
                GappleFinderClient.updatePos = true;
                GappleFinderClient.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
                GappleFinderClient.baritoneTargetChestPos = null;
                GappleFinderClient.finderEnabled = true;
                source.sendFeedback(Text.literal("Gapple: finder started!"));
                return 1;
            }
        }
        else if (!GappleFinderClient.finderEnabled) {
            GappleFinderClient.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            GappleFinderClient.baritoneTargetChestPos = null;
            GappleFinderClient.finderEnabled = true;
            source.sendFeedback(Text.literal("Gapple: finder started!"));
            return 1;
        }
        source.sendFeedback(Text.literal("Gapple: error! finder already running!"));
        return 0;
        //source.sendFeedback(Text.literal(),true);
    }
    private static int stop(FabricClientCommandSource source) throws CommandSyntaxException {
        if (GappleFinderClient.finderEnabled) {
            if (IntegratedServerManager.integratedServerRunning) {
                GappleFinderClient.updatePos = false;
                IntegratedServerManager.server.stop(false);
                IntegratedServerManager.integratedServerRunning = false;
                GappleFinderClient.baritone.getPathingBehavior().cancelEverything();
            }
            source.sendFeedback(Text.literal("Gapple: finder stopped!"));
            GappleFinderClient.finderEnabled = false;
            return 1;
        }
        source.sendFeedback(Text.literal("Gapple: error! finder already stopped!"));
        return 0;
    }
    private static int auto(FabricClientCommandSource source) throws CommandSyntaxException {
        if (GappleFinderClient.enableBaritone) {
            GappleFinderClient.enableBaritone = false;
            GappleFinderClient.baritoneTargetChestPos = null;
            source.sendFeedback(Text.literal("Gapple: baritone toggled off!"));
            GappleFinderClient.baritone.getPathingBehavior().cancelEverything();
        } else {
            GappleFinderClient.enableBaritone = true;
            source.sendFeedback(Text.literal("Gapple: baritone toggled on!"));
        }
        return 1;
    }

    private static int setSearchDistance(FabricClientCommandSource source, int radius) {
        GappleFinderClient.searchRadius = radius;
        source.sendFeedback(Text.literal("Gapple: set multiplayer search distance to "+radius+"!"));
        return 1;
    }

    private static int help(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("<Gapple Finder 2 by Captain_S0L0>"));
        source.sendFeedback(Text.literal(""));
        source.sendFeedback(Text.literal("Note: To use on a server, you must create a singleplayer world named \"gapple\" with the seed and datapacks of the server."));
        source.sendFeedback(Text.literal("If you do not know / cannot get the seed and datapacks of the server, then you're out of luck, mate."));
        source.sendFeedback(Text.literal(""));
        source.sendFeedback(Text.literal("Commands:"));
        source.sendFeedback(Text.literal("/gapple start : enable finder"));
        source.sendFeedback(Text.literal("/gapple stop : disable finder"));
        source.sendFeedback(Text.literal("/gapple help : print this list"));
        source.sendFeedback(Text.literal("/gapple auto : toggle baritone automation"));
        source.sendFeedback(Text.literal("/gapple searchDistance <radius> : set search radius (in chunks)"));
    }


}
