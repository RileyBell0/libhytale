package com.example.plugin.interfaces;

import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class TickingBlockComponent_Initialiser extends RefSystem<ChunkStore> {

    private Query<ChunkStore> query;

    public TickingBlockComponent_Initialiser(Query<ChunkStore> query) {
        this.query = query;
    }

    protected TickingBlockComponent_Initialiser() {}

    /**
     * If you have blocks that you want to "tick", you need to "initialise" them
     * when they are loaded into the world or placed etc
     */
    @Override
    public void onEntityAdded(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        BlockUtils.setTicking(commandBuffer, ref);
    }

    @Override
    public void onEntityRemove(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {}

    // Example: I override `getQuery` and use the following
    // `return Query.and(BlockModule.BlockStateInfo.getComponentType(), ExampleTickingComponent.getComponentType());`
    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
