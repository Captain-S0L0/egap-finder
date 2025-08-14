package com.terriblefriends.gapplefinder.mixin;

import com.terriblefriends.gapplefinder.access.StorageMinecartEntityInterface;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StorageMinecartEntity.class)
public class StorageMinecartEntityMixin implements StorageMinecartEntityInterface {
    @Shadow
    protected long lootSeed;
    @Shadow
    protected Identifier lootTableId;

    public long getLootTableSeed() {return lootSeed;}
    public Identifier getLootTableId() {return lootTableId;}
}
