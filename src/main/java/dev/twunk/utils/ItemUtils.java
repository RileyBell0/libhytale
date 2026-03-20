package dev.twunk.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import javax.annotation.Nonnull;

public abstract class ItemUtils extends com.hypixel.hytale.server.core.entity.ItemUtils {

    // This is a "stack" of items, i believe all items should be considered a stack, really
    // hoping that a single item is just a stack of "1" items
    private static final Class<?> Item = ItemStack.class;

    // This seems to contain all the stuff i already wanted working
    //
    // Notably, hytale seems to have really focussed in on entities and general
    // interactions like THIS and not the backend logic of blocks/interactions in
    // that regard. This entity code seems way more polished than literally anything
    // related to chunks and blocks and the world itself
    private static final Class<?> InvHelper = InventoryHelper.class;

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
