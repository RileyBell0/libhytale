package dev.twunk.interfaces.methods;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

/**
 * Methods for my subsytem version of `ChunkBlockTickSystem.Ticking`
 * @see com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem a waking nightmare
 *
 * Good for
 * - auto-ticking block components
 * - systems that tick blocks
 * - etc
 *
 * TODO:
 * - overall goal here is to provide a consistent (and hopefully smaller) interface
 *   for interacting with block components.
 * - Might consider making a `blockRef` class that extends `Ref<ChunkStore>` that i
 *   can instead store all these things in INDIRECTLY rather than computing them for
 *   everything. not all `onBlockTick` implementations will require all these, so its
 *   just wasted compute
 */
public interface IBlockTick {
    public void onBlockTick(
        final @Nonnull Ref<ChunkStore> blockRef,
        final @Nonnull World world,
        final @Nonnull WorldChunk wc,
        final @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        final @Nonnull Vector3i worldCoords,
        final int blockId
    );
}
