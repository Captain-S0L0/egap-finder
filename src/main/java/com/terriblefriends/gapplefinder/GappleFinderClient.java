package com.terriblefriends.gapplefinder;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.utils.BetterBlockPos;
import com.terriblefriends.gapplefinder.access.LootableContainerBlockEntityInterface;
import com.terriblefriends.gapplefinder.access.StorageMinecartEntityInterface;
import com.terriblefriends.gapplefinder.commands.GappleCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;

import java.util.*;

public class GappleFinderClient implements ClientModInitializer {
    public static boolean updatePos = false;
    public static boolean enableBaritone = false;
    public static IBaritone baritone;
    public static List<BlockPos> chestList = new ArrayList<>();
    public static BlockPos baritoneTargetChestPos;
    public static MinecraftClient client;
    public static boolean finderEnabled = false;
    public static ChunkPos toLoadPos = null;
    public static RegistryKey<World> toLoadDimension = null;
    public static int searchRadius = 24;

    //TODO: fix compatibility with redirects in ChatScreenMixin with the bedrock breaking client
    //TODO: remove chests on singleplayer worlds

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register((client -> {
            ClientPlayerEntity player = client.player;
            if ((updatePos || finderEnabled) && client.player == null) {
                updatePos = false;
                finderEnabled = false;
                if (IntegratedServerManager.integratedServerRunning) {
                    IntegratedServerManager.server.stop(false);
                    IntegratedServerManager.integratedServerRunning = false;
                }
                Messager.rawchat("detected disconnect, stopped finder!");
                return;
            }
            if (updatePos) {
                toLoadPos = new ChunkPos(new BlockPos(client.player.getBlockPos()));
                toLoadDimension = client.player.world.getRegistryKey();
            }
            if (finderEnabled) {
                if (player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                    Inventory inventory = ((GenericContainerScreenHandler) player.currentScreenHandler).getInventory();
                    boolean gappleChest = false;
                    for (int counter1 = 0; counter1 < (inventory.size()); counter1++) {
                        if (inventory.getStack(counter1).getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                            if (enableBaritone) {
                                client.interactionManager.clickSlot(player.currentScreenHandler.syncId, counter1, 0, SlotActionType.QUICK_MOVE, player);
                            }
                            gappleChest = true;
                        }
                    }
                    if (gappleChest) {
                        if (enableBaritone && chestList.contains(baritoneTargetChestPos)) {
                            if (IntegratedServerManager.integratedServerRunning) {
                                IntegratedServerManager.removeChest(baritoneTargetChestPos, toLoadDimension);
                            }
                            removeChest(baritoneTargetChestPos);
                            baritoneTargetChestPos = null;
                            //Messager.rawchat("chest removed baritone targets");
                            return;
                        }
                        //if (client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                        BlockPos targetedBlock = new BlockPos(client.crosshairTarget.getPos());
                        //Messager.rawchat(targetedBlock.toShortString());
                        if (chestList.contains(targetedBlock)) {
                            if (IntegratedServerManager.integratedServerRunning) {
                                IntegratedServerManager.removeChest(targetedBlock, toLoadDimension);
                            }
                            removeChest(targetedBlock);
                            //Messager.rawchat("chest removed chest list");
                            return;
                        }
                        //}
                        /*if (client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                            BlockPos targetedBlock = new BlockPos(client.crosshairTarget.getPos());
                            if (chestList.contains(targetedBlock)) {
                                IntegratedServerManager.removeChest(targetedBlock, toLoadDimension);
                                removeChest(targetedBlock);
                                Messager.rawchat("chest removed chest list");
                                return;
                            }
                        }*/
                    }
                }
            }
            if (enableBaritone) {
                if (baritoneTargetChestPos != null && player.squaredDistanceTo(baritoneTargetChestPos.getX(), baritoneTargetChestPos.getY(), baritoneTargetChestPos.getZ()) < 4) {
                    baritone.getPathingBehavior().cancelEverything();
                    if (!(player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                        List carts = client.world.getEntitiesByClass(StorageMinecartEntity.class, new Box(baritoneTargetChestPos), EntityPredicates.VALID_ENTITY);
                        if (carts.size() > 0 && carts.get(0) != null) {
                            client.interactionManager.interactEntity(client.player, (Entity) carts.get(0), player.getActiveHand());
                        } else {
                            client.interactionManager.interactBlock(client.player, player.getActiveHand(), new BlockHitResult(
                                    Vec3d.ofCenter(new BlockPos(baritoneTargetChestPos.getX(), baritoneTargetChestPos.getY(), baritoneTargetChestPos.getZ())),
                                    Direction.DOWN,
                                    baritoneTargetChestPos,
                                    false));

                        }
                    }
                }
                if (chestList.size() == 0 && !(baritone.getPathingBehavior().getGoal() instanceof GoalXZ)) {
                    baritone.getCustomGoalProcess().setGoalAndPath(new GoalXZ(new BetterBlockPos(29999900, 62, player.getZ())));
                }
                if (chestList.size() > 0 && !(baritone.getPathingBehavior().getGoal() instanceof GoalGetToBlock) && baritoneTargetChestPos == null) {
                    baritone.getPathingBehavior().cancelEverything();
                    Iterator iterator = chestList.iterator();
                    double distance = Double.MAX_VALUE;
                    BlockPos nextchest = BlockPos.ORIGIN;
                    BlockPos tocheck;
                    while (iterator.hasNext()) {
                        tocheck = (BlockPos) iterator.next();
                        //System.out.println(tocheck);
                        //System.out.println(player.squaredDistanceTo(tocheck.getX(), tocheck.getY(), tocheck.getZ()));
                        if (player.squaredDistanceTo(tocheck.getX(), tocheck.getY(), tocheck.getZ()) < distance) {
                            nextchest = tocheck;
                            distance = player.squaredDistanceTo(tocheck.getX(), tocheck.getY(), tocheck.getZ());
                            //System.out.println("distance " + distance + " " + nextchest);
                        }
                    }
                    baritone.getCustomGoalProcess().setGoalAndPath(new GoalGetToBlock(nextchest));
                    baritoneTargetChestPos = nextchest;
                }
            }
        }));

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
            if (blockEntity instanceof LootableContainerBlockEntity) {
                chestChecker(blockEntity,world);
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ChestMinecartEntity) {
                minecartChecker(entity,world);
            }
        });
        ServerTickEvents.START_SERVER_TICK.register((server -> {
            if (toLoadPos != null) {
                server.getWorld(toLoadDimension).getChunkManager().addTicket(ChunkTicketType.UNKNOWN, toLoadPos, searchRadius, toLoadPos);
            }
        }));

        ClientLifecycleEvents.CLIENT_STARTED.register((client1) -> {
            client = client1;
        });

        ClientCommandRegistrationCallback.EVENT.register(GappleCommand::register);
    }

    private void chestChecker(BlockEntity blockEntity, ServerWorld world) {
        if (!finderEnabled) {return;}
        long lootTableSeed = ((LootableContainerBlockEntityInterface)blockEntity).getLootTableSeed();
        Identifier lootTableId = ((LootableContainerBlockEntityInterface)blockEntity).getLootTableId();
        LootTable lootTable = world.getServer().getLootManager().getTable(lootTableId);
        LootContext.Builder builder = (new LootContext.Builder(world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockEntity.getPos())).random(lootTableSeed);
        List<ItemStack> generated = lootTable.generateLoot(builder.build(LootContextTypes.CHEST));
        for (ItemStack itemStack : generated) {
            if (itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                Messager.rawchat("gapple found! " + blockEntity.getPos().getX() + " " + blockEntity.getPos().getY() + " " + blockEntity.getPos().getZ());
                addChest(blockEntity.getPos());
                return;
            }
        }
    }

    private void minecartChecker(Entity entity, ServerWorld world) {
        if (!finderEnabled) {return;}
        long lootTableSeed = ((StorageMinecartEntityInterface)entity).getLootTableSeed();
        Identifier lootTableId = ((StorageMinecartEntityInterface)entity).getLootTableId();
        LootTable lootTable = world.getServer().getLootManager().getTable(lootTableId);
        LootContext.Builder builder = new LootContext.Builder(world).parameter(LootContextParameters.ORIGIN,entity.getPos()).random(lootTableSeed);
        List<ItemStack> generated = lootTable.generateLoot(builder.build(LootContextTypes.CHEST));
        for (ItemStack itemStack : generated) {
            if (itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                Messager.rawchat("gapple found! " + entity.getPos().getX() + " " + entity.getPos().getY() + " " + entity.getPos().getZ());
                addChest(entity.getBlockPos());
                return;
            }
        }
    }

    private static void addChest(BlockPos pos) {
        if (!chestList.contains(pos)) {
            chestList.add(pos);
        }
    }

    private static void removeChest(BlockPos pos) {
        chestList.remove(pos);
    }
}
