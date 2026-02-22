package dev.twunk.utils.tick;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public interface ITickableBlockEntity {
    /**
     * Ticking a block? Just need some damn code to run in game while testing? put
     * it in here!
     *
     * NOTE: once you're done testing, move this across into a System to handle
     * this the way we're actually meant to be handling this
     *
     * remember: components don't tick, systems do, and systems "just so happen"
     * to be able to filter themselves down to entites with your component
     *
     * instead of "i want my component to tick", nah, you want a SYSTEM that
     * queries just your component to tick
     */
    public void onBlockEntityTick(
        @Nonnull Ref<ChunkStore> blockRef,
        @Nonnull World world,
        @Nonnull WorldChunk wc,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull Vector3i worldCoords,
        int blockId
    );
}
