package com.terriblefriends.gapplefinder.access;

import net.minecraft.util.Identifier;

public interface LootableContainerBlockEntityInterface {
    public Identifier getLootTableId();
    public long getLootTableSeed();
}
