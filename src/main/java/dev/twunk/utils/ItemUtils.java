package dev.twunk.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public abstract class ItemUtils {

    @SuppressWarnings("unused")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public static final void test(
        @Nonnull final Ref<ChunkStore> blockRef,
        @Nonnull final WorldChunk worldChunk,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull final Vector3i providedCoords
    ) {
        // final var blockX = providedCoords.x;
        // final var blockY = providedCoords.y;
        // final var blockZ = providedCoords.z;
        // final var index = Coords.Index.get(blockX, blockY, blockZ);
        // final var localCoords = Coords.Local.get(index);
        // final var blockCoords = new Vector3i(blockX, blockY, blockZ);
        // final var test = new TestUtil(commandBuffer, blockCoords);
        // var local = Coords.Local.get(test.blockRef);
        // if (local == null || !local.equals(localCoords)) {
        //     throw new RuntimeException("Failed to convert blockRef to localCoords");
        // }
    }
}
