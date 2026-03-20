package dev.twunk.interfaces.methods;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

/**
 * Means: I've got a method that can be called for ticking blocks.
 *
 * Good for
 * - auto-ticking block components
 * - systems that tick blocks
 * - etc
 *
 * just giving a consistent interface for interfacing with block entities
 */
public interface IBlockTick {
    public void onBlockTick(
        @Nonnull Ref<ChunkStore> blockRef,
        @Nonnull World world,
        @Nonnull WorldChunk wc,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull Vector3i worldCoords,
        int blockId
    );
}
