package com.example.plugin;

import com.example.plugin.structs.ExampleBlock;
import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class TickingInitializer extends RefSystem<ChunkStore> {

    /**
     * When blocks are loaded, we need to mark them as "ticking" ourselves
     *
     * @param ref
     * @param reason
     * @param store
     * @param commandBuffer
     */
    @Override
    public void onEntityAdded(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        var info = BlockUtils.getInfo(commandBuffer, ref);
        if (info == null) return;

        var coords = BlockUtils.getCoordsInChunk(info);

        var worldChunkComponentType = WorldChunk.getComponentType();
        if (worldChunkComponentType == null) {
            return;
        }
        WorldChunk worldChunk = (WorldChunk) commandBuffer.getComponent(info.getChunkRef(), worldChunkComponentType);
        if (worldChunk != null) {
            BlockUtils.setTicking(worldChunk, coords);
            // worldChunk.setTicking(coords.x, coords.y, coords.z, true);
        }
    }

    @Override
    public void onEntityRemove(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {}

    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(BlockModule.BlockStateInfo.getComponentType(), ExampleBlock.getComponentType());
    }
}
