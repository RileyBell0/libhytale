package com.example.plugin.structs;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public interface TickingBlockEntity extends Component<ChunkStore> {
    static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    // Ticking a block or entity? put it in here!
    @Nonnull
    public default BlockTickStrategy onTick(
        @Nonnull World world,
        @Nonnull WorldChunk wc,
        int worldX,
        int worldY,
        int worldZ,
        int blockId
    ) {
        console.log("Ticked block at (" + worldX + ", " + worldY + ", " + worldZ + " )");

        return BlockTickStrategy.CONTINUE;
    }
}
