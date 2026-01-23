package com.example.plugin;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class Utils {

    // This is a constant i need for checking if something its touching is a
    // container
    @SuppressWarnings("removal")
    public static final ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_TYPE = BlockStateModule.get()
            .getComponentType(ItemContainerState.class);
}
