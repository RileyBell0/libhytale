package com.example.plugin.interfaces;

import com.example.plugin.examples.ExampleTickingBlockComponent;
import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class TickingBlockComponent_Initialiser extends RefSystem<ChunkStore> {

    private Query<ChunkStore> query;

    public TickingBlockComponent_Initialiser(Query<ChunkStore> query) {
        this.query = query;
    }

    // easy way to construct: if you pass in your class itself i'll just hook stuff
    // up with a default query
    public TickingBlockComponent_Initialiser(Class<? extends TickingBlockComponent> componentClass) {
        this.query = getDefaultQuery(componentClass);
    }

    protected static Query<ChunkStore> getDefaultQuery(Class<? extends TickingBlockComponent> componentClass) {
        return Query.and(
            BlockModule.BlockStateInfo.getComponentType(),
            TickingBlockComponent.getComponentType(ExampleTickingBlockComponent.class)
        );
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
    ) {
        // BlockUtils.setTicking(commandBuffer, ref, false);
    }

    // Example: I override `getQuery` and use the following
    // `return Query.and(BlockModule.BlockStateInfo.getComponentType(), ExampleTickingComponent.getComponentType());`
    //
    // and since that seems to be a common pattern, i've made a constructor that you can just pass your class
    // to, and given you've actually registered your component it'll "just work"
    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
