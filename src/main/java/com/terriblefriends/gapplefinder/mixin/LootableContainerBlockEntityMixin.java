package com.terriblefriends.gapplefinder.mixin;

import com.terriblefriends.gapplefinder.access.LootableContainerBlockEntityInterface;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootableContainerBlockEntity.class)
public class LootableContainerBlockEntityMixin implements LootableContainerBlockEntityInterface {
    @Shadow
    protected Identifier lootTableId;
    @Shadow
    protected long lootTableSeed;
    public Identifier getLootTableId() {return lootTableId;}
    public long getLootTableSeed() {return lootTableSeed;}
}
