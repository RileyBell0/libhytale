package dev.twunk.component;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.system.response.TickResponse;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// "BlockType": {
//   "TickProcedure": {
//     "Type": "InherentTickProcedure"
//   },
//   ...
// }
// ^^ the above is an option. I dont like it but it's an option

public interface ITickingComponent extends IRegisteredComponent {
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
    @Nullable
    public default TickResponse onTick(
        @Nonnull World world,
        @Nonnull WorldChunk wc,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        int worldX,
        int worldY,
        int worldZ,
        int blockId
    ) {
        HytaleLogger.forEnclosingClass()
            .atInfo()
            .log("Ticked block at (" + worldX + ", " + worldY + ", " + worldZ + " )");
        return null;
    }
}
