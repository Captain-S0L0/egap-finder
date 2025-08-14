package com.terriblefriends.gapplefinder;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Clearable;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class IntegratedServerManager {

    private static Logger LOGGER = LogUtils.getLogger();
    public @Nullable static MinecraftServer server;
    public static boolean integratedServerRunning = false;

    public static void startServer() {
        Messager.rawchat("starting internal server, this will lag a bit!");

        String worldName = "gapple";

        net.minecraft.world.level.storage.LevelStorage.Session session;
        try {
            session = GappleFinderClient.client.getLevelStorage().createSession(worldName);
        } catch (IOException var22) {
            Messager.rawchat("error! you need to create a world named \"gapple\", with the seed & datapacks (if they modify loot tables) of the world you wish to search to use the finder!");
            LOGGER.warn("Failed to read level {} data", worldName, var22);
            return;
        }

        ResourcePackManager resourcePackManager = new ResourcePackManager(ResourceType.SERVER_DATA, new VanillaDataPackProvider(), new FileResourcePackProvider(session.getDirectory(WorldSavePath.DATAPACKS).toFile(), ResourcePackSource.PACK_SOURCE_WORLD));

        //private void startIntegratedServer(String worldName, Function<net.minecraft.world.level.storage.LevelStorage.Session, DataPackSettingsSupplier> dataPackSettingsSupplierGetter,
        // Function<net.minecraft.world.level.storage.LevelStorage.Session, SavePropertiesSupplier> savePropertiesSupplierGetter, boolean safeMode, MinecraftClient.WorldLoadAction worldLoadAction) {

        /*public void startIntegratedServer(String worldName) {
            this.startIntegratedServer(worldName, DataPackSettingsSupplier::loadFromWorld, SavePropertiesSupplier::loadFromWorld, false, MinecraftClient.WorldLoadAction.BACKUP);
        }*/


        SaveLoader saveLoader;


        //Function<net.minecraft.world.level.storage.LevelStorage.Session, SaveLoader.DataPackSettingsSupplier> dataPackSettingsSupplierGetter = SaveLoader.DataPackSettingsSupplier::loadFromWorld;
        //Function<net.minecraft.world.level.storage.LevelStorage.Session, SaveLoader.SavePropertiesSupplier> savePropertiesSupplierGetter = SaveLoader.SavePropertiesSupplier::loadFromWorld;
        AtomicReference<WorldGenerationProgressLogger> worldGenProgressLogger = new AtomicReference();

        try {
            //saveLoader = GappleFinderClient.client.createSaveLoader(resourcePackManager, false, dataPackSettingsSupplierGetter.apply(session), savePropertiesSupplierGetter.apply(session));
            saveLoader = GappleFinderClient.client.createIntegratedServerLoader().createSaveLoader(session, false);
        } catch (Exception var21) {
            Messager.rawchat("error! failed to load datapacks!");
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", var21);
            try {
                //resourcePackManager.close();
                session.close();
            } catch (IOException var17) {
                LOGGER.warn("Failed to unlock access to level {}", worldName, var17);
            }
            return;
        }

        SaveProperties exception = saveLoader.saveProperties();
        boolean iOException2 = exception.getGeneratorOptions().isLegacyCustomizedType();
        boolean bl = exception.getLifecycle() != Lifecycle.stable();
        if (!iOException2 && !bl) {
            //log("you are using experimental data packs! I don't know if this will break anything, but here we go anyways!");
        }
            try {
                //saveLoader.refresh();
                YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(GappleFinderClient.client.getNetworkProxy());
                GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
                //UserCache userCache = new UserCache(gameProfileRepository, new File(GappleFinderClient.client.runDirectory, MinecraftServer.USER_CACHE_FILE.getName()));
                //UserCache userCache = new UserCache(gameProfileRepository, new File(GappleFinderClient.client.runDirectory, MinecraftServer.));
                //userCache.setExecutor(GappleFinderClient.client);
                //SkullBlockEntity.setServices(userCache, minecraftSessionService, GappleFinderClient.client);

                session.backupLevelDataFile(saveLoader.dynamicRegistryManager(), saveLoader.saveProperties());
                ApiServices apiServices = ApiServices.create(yggdrasilAuthenticationService, GappleFinderClient.client.runDirectory);
                apiServices.userCache().setExecutor(GappleFinderClient.client);

                UserCache.setUseRemote(false);
                server = MinecraftServer.startServer((thread2) -> new IntegratedServer(thread2, GappleFinderClient.client, session, resourcePackManager, saveLoader, apiServices, (spawnChunkRadius) -> {
                    WorldGenerationProgressLogger worldGenerationProgressLogger = new WorldGenerationProgressLogger(0);
                    worldGenProgressLogger.set(worldGenerationProgressLogger);
                    worldGenerationProgressLogger.start();
                    return worldGenerationProgressLogger;
                }));
                integratedServerRunning = true;
                Messager.rawchat("internal server started!");
            } catch (Throwable var20) {
                CrashReport yggdrasilAuthenticationService = CrashReport.create(var20, "Starting integrated server");
                CrashReportSection minecraftSessionService = yggdrasilAuthenticationService.addElement("Starting integrated server");
                minecraftSessionService.add("Level ID", worldName);
                minecraftSessionService.add("Level Name", exception.getLevelName());
                throw new CrashException(yggdrasilAuthenticationService);
            }

            /*while(worldGenProgressLogger.get() == null) {
                Thread.yield();
            }*/

            /*while(!server.isLoading()) {
                try {
                    Thread.sleep(16L);
                } catch (InterruptedException ignored) { }
            }*/

        /*Function<net.minecraft.world.level.storage.LevelStorage.Session, SaveLoader.SavePropertiesSupplier> savePropertiesSupplierGetter = (session2) -> {
            return (resourceManager, dataPackSettings) -> {
                DynamicRegistryManager.Mutable mutable = DynamicRegistryManager.createAndLoad();
                DynamicOps<JsonElement> dynamicOps = RegistryOps.of(JsonOps.INSTANCE, new MoreOptionsDialog);
                DynamicOps<JsonElement> dynamicOps2 = RegistryOps.ofLoaded(JsonOps.INSTANCE, mutable, resourceManager);
                DataResult<GeneratorOptions> dataResult = GeneratorOptions.CODEC.encodeStart(dynamicOps, GeneratorOptions.getDefaultOptions(immutable)).setLifecycle(Lifecycle.stable()).flatMap((json) -> {
                    return GeneratorOptions.CODEC.parse(dynamicOps2, json);
                });
                Logger var10003 = LogUtils.getLogger();
                GeneratorOptions generatorOptions2 = (GeneratorOptions) dataResult.getOrThrow(false, Util.addPrefix("Error reading worldgen settings after loading data packs: ", var10003::error));
                return Pair.of(new LevelProperties(levelInfo, generatorOptions2, dataResult.lifecycle()), mutable.toImmutable());
            };
        };*/
    }

    public static void removeChest(BlockPos pos, RegistryKey<World> dimension) {
        List carts = server.getWorld(dimension).getEntitiesByClass(StorageMinecartEntity.class, new Box(pos).expand(2,2,2), EntityPredicates.VALID_ENTITY);
        for (Object e : carts) {
            if (e instanceof StorageMinecartEntity) {
                ((Entity)e).kill();
            }
        }
        if (server.getWorld(dimension).getBlockState(pos).getBlock() == Blocks.CHEST) {
            Clearable.clear(server.getWorld(dimension).getBlockEntity(pos));
            server.getWorld(dimension).setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }
}
