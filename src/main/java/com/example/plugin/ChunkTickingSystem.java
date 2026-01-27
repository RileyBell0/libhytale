package com.example.plugin;

import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ChunkTickingSystem extends ChunkBlockTickSystem.Ticking {

    @Nullable
    protected static BlockTickStrategy tickBlock(@Nonnull CommandBuffer<ChunkStore> cmd, @Nonnull Ref<ChunkStore> ref) {
        var info = BlockUtils.getInfo(cmd, ref);
        if (info == null) {
            return null;
        }

        // get the chunk the block is in
        var worldChunk = BlockUtils.getWorldChunk(cmd, info);
        if (worldChunk == null) {
            return null;
        }

        // and the world of said block
        var world = worldChunk.getWorld();
        if (world == null) {
            return null;
        }

        // and its associated coords
        var coords = BlockUtils.getCoordsInChunk(info);

        return tickProcedure(world, worldChunk, coords.x, coords.y, coords.z, worldChunk.getBlock(coords));
    }
}
