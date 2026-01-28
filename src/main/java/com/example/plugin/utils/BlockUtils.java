package com.example.plugin.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// @Nullable
// public static Ref<ChunkStore> getBlockEntity(@Nonnull World world, int x, int y, int z) {
//     ChunkStore chunkStore = world.getChunkStore();
//     Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
//     if (chunkRef == null) {
//         return null;
//     } else {
//         BlockComponentChunk blockComponentChunk = chunkStore
//             .getStore()
//             .getComponent(chunkRef, BlockComponentChunk.getComponentType());
//         if (blockComponentChunk == null) {
//             return null;
//         } else {
//             int blockIndex = ChunkUtil.indexBlockInColumn(x, y, z);
//             Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
//             return blockRef != null && blockRef.isValid() ? blockRef : null;
//         }
//     }
// }

// @Nullable
// public <T extends Component<ChunkStore>> T getComponent(
//     ComponentType<ChunkStore, T> componentType,
//     World world,
//     int x,
//     int y,
//     int z
// ) {
//     Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
//     Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
//     BlockComponentChunk blockComponentChunk = chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
//     if (blockComponentChunk == null) {
//         return null;
//     } else {
//         int blockIndex = ChunkUtil.indexBlockInColumn(x, y, z);
//         Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
//         return blockRef != null && blockRef.isValid() ? chunkStore.getComponent(blockRef, componentType) : null;
//     }
// }

// @Nullable
// public Ref<ChunkStore> getChunkSectionReference(int x, int y, int z) {
//     Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
//     if (ref == null) {
//         return null;
//     } else {
//         ChunkColumn chunkColumnComponent = this.store.getComponent(ref, ChunkColumn.getComponentType());
//         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
//     }
// }

// @Nullable
// public Ref<ChunkStore> getChunkSectionReference(
//     @Nonnull ComponentAccessor<ChunkStore> commandBuffer,
//     int x,
//     int y,
//     int z
// ) {
//     Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
//     if (ref == null) {
//         return null;
//     } else {
//         ChunkColumn chunkColumnComponent = commandBuffer.getComponent(ref, ChunkColumn.getComponentType());
//         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
//     }
// }

// public static void setTickingSurrounding(
//     @Nonnull FluidTicker.Accessor accessor,
//     BlockSection blockSection,
//     int worldX,
//     int worldY,
//     int worldZ
// ) {
//     for (int y = -1; y <= 1; y++) {
//         for (int z = -1; z <= 1; z++) {
//             for (int x = -1; x <= 1; x++) {
//                 int bx = worldX + x;
//                 int by = worldY + y;
//                 int bz = worldZ + z;
//                 // RILEY READ THIS TODO TOOD
//                 // TODO READ THIS RILEY
//                 BlockSection chunk = ChunkUtil.isSameChunkSection(worldX, worldY, worldZ, bx, by, bz)
//                     ? blockSection
//                     : accessor.getBlockSectionByBlock(bx, by, bz);
//                 if (chunk != null) {
//                     chunk.setTicking(bx, by, bz, true);
//                 }
//             }
//         }
//     }
// }

// Utils for blocks. Slowly figuring out what this should look like
// NOTE - its current state is broken
public class BlockUtils {

    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    @Nullable
    public static BlockModule.BlockStateInfo getInfo(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull Ref<ChunkStore> ref
    ) {
        var blockInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
        if (blockInfoComponentType == null) {
            return null;
        }

        // We want the "info" component from the block
        // -> this is how we find out the coords
        var info = (BlockModule.BlockStateInfo) commandBuffer.getComponent(ref, blockInfoComponentType);
        if (info == null) {
            return null;
        }

        return info;
    }

    public static BlockModule.BlockStateInfo getInfo(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull BlockComponentChunk chunk,
        int localX,
        int localY,
        int localZ
    ) {
        var ref = BlockUtils.getRef(chunk, localX, localY, localZ);
        if (ref == null) {
            return null;
        }
        return BlockUtils.getInfo(commandBuffer, ref);
    }

    // get the chunk for a given block
    @Nullable
    public static WorldChunk getWorldChunk(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull Ref<ChunkStore> ref
    ) {
        var info = BlockUtils.getInfo(commandBuffer, ref);
        if (info == null) {
            return null;
        }

        var chunkComponentType = WorldChunk.getComponentType();
        if (chunkComponentType == null) {
            return null;
        }

        return commandBuffer.getComponent(info.getChunkRef(), chunkComponentType);
    }

    // get the chunk for a given block
    @Nullable
    public static WorldChunk getWorldChunk(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull BlockModule.BlockStateInfo info
    ) {
        var chunkComponentType = WorldChunk.getComponentType();
        if (chunkComponentType == null) {
            return null;
        }

        return commandBuffer.getComponent(info.getChunkRef(), chunkComponentType);
    }

    // Get the local coords of the block in its chunk
    public static Vector3i getLocalCoords(@Nonnull BlockModule.BlockStateInfo info) {
        var indexInChunk = info.getIndex();
        int x = ChunkUtil.xFromBlockInColumn(indexInChunk);
        int y = ChunkUtil.yFromBlockInColumn(indexInChunk);
        int z = ChunkUtil.zFromBlockInColumn(indexInChunk);

        return new Vector3i(x, y, z);
    }

    // get the chunk for a given block
    @Nullable
    public static Vector3i getGlobalCoords(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull BlockModule.BlockStateInfo info
    ) {
        var chunk = BlockUtils.getWorldChunk(commandBuffer, info);
        if (chunk == null) {
            return null;
        }

        var localCoords = BlockUtils.getLocalCoords(info);
        return BlockUtils.toGlobalCoords(chunk, localCoords);
    }

    @Nonnull
    public static Vector3i toGlobalCoords(@Nonnull WorldChunk chunk, int localX, int localY, int localZ) {
        int globalX = localX + (chunk.getX() * 32);
        int globalZ = localZ + (chunk.getZ() * 32);

        return new Vector3i(globalX, localY, globalZ);
    }

    public static Vector3i toGlobalCoords(@Nonnull WorldChunk chunk, @Nonnull Vector3i localCoords) {
        return BlockUtils.toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
    }

    // Get the local coords of the block in its chunk
    public static Vector3i getLocalCoords(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        var info = BlockUtils.getInfo(commandBuffer, ref);
        if (info == null) {
            return null;
        }

        return BlockUtils.getLocalCoords(info);
    }

    // public static Ref<ChunkStore> get(@Nonnull World world, int globalX, int globalY, int globalZ) {
    //     var worldChunk = world.getChunkAsync(ChunkUtil.indexChunkFromBlock(globalX, globalZ));
    //     var blockRef = worldChunk.thenApply(chunk -> chunk.getBlockComponentEntity(globalX, globalY, globalZ));

    //     return blockRef;
    // }

    // public static CompletableFuture<Ref<ChunkStore>[]> getTouching(@Nonnull World world, int x, int y, int z) {
    //     var future = new CompletableFuture<Ref<ChunkStore>[]>();

    //     @SuppressWarnings("unchecked")
    //     Ref<ChunkStore>[] items = new Ref[6];

    //     var block0 = BlockUtils.get(world, x, y, z + 1);
    //     var block1 = BlockUtils.get(world, x, y, z - 1);
    //     var block2 = BlockUtils.get(world, x, y + 1, z);
    //     var block3 = BlockUtils.get(world, x, y - 1, z);
    //     var block4 = BlockUtils.get(world, x + 1, y, z);
    //     var block5 = BlockUtils.get(world, x - 1, y, z);
    //     CompletableFuture.allOf(block0, block1, block2, block3, block4, block5).thenRun(() -> {
    //         items[0] = block0.join();
    //         items[1] = block1.join();
    //         items[2] = block2.join();
    //         items[3] = block3.join();
    //         items[4] = block4.join();
    //         items[5] = block5.join();

    //         future.complete(items);
    //     });

    //     return future;
    // }

    public static boolean setTicking(@Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull Ref<ChunkStore> ref) {
        return BlockUtils.setTicking(commandBuffer, ref, true);
    }

    public static boolean setTicking(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull Ref<ChunkStore> ref,
        boolean ticking
    ) {
        var info = BlockUtils.getInfo(commandBuffer, ref);
        if (info == null) {
            console.log("Info was null");
            return false;
        }

        return BlockUtils.setTicking(commandBuffer, info, ticking);
    }

    public static boolean setTicking(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull BlockModule.BlockStateInfo info
    ) {
        return BlockUtils.setTicking(commandBuffer, info, true);
    }

    public static boolean setTicking(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull BlockModule.BlockStateInfo info,
        boolean ticking
    ) {
        var worldChunk = BlockUtils.getWorldChunk(commandBuffer, info);
        if (worldChunk == null) {
            console.log("World chunk was null");
            return false;
        }

        var coords = BlockUtils.getLocalCoords(info);
        return BlockUtils.setTicking(worldChunk, coords, ticking);
    }

    public static boolean setTicking(@Nonnull WorldChunk worldChunk, @Nonnull Vector3i coords) {
        return BlockUtils.setTicking(worldChunk, coords, true);
    }

    public static boolean setTicking(@Nonnull WorldChunk worldChunk, @Nonnull Vector3i coords, boolean ticking) {
        return worldChunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }

    public static Ref<ChunkStore> getRef(@Nonnull BlockComponentChunk chunk, int localX, int localY, int localZ) {
        return chunk.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
    }

    public static <T extends Component<ChunkStore>> T getComponent(
        @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull BlockComponentChunk chunk,
        int localX,
        int localY,
        int localZ
    ) {
        var ref = BlockUtils.getRef(chunk, localX, localY, localZ);
        if (ref == null) {
            return null;
        }

        return commandBuffer.getComponent(ref, getComponentType.get());
    }

    public static <T extends Component<ChunkStore>> T getComponent(
        @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull Ref<ChunkStore> ref
    ) {
        return commandBuffer.getComponent(ref, getComponentType.get());
    }

    public static <T extends Component<ChunkStore>> boolean hasComponent(
        @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull Ref<ChunkStore> ref
    ) {
        return (T) commandBuffer.getComponent(ref, getComponentType.get()) != null;
    }

    /**
     * Gets the "BlockType" for a block with the given Id.
     *
     * Note that the required `blockId` is the ID of the block that YOU SET in your
     * `resources/Server/Item/Items/RileysBlock.json` in the `Id` at the base of
     * the json object
     *
     * @param blockId The stringy ID you chose for your BLOCK asset in the
     *                `resources/Server/Item/Items/<>.json` file
     */
    public static BlockType getBlockType(@Nonnull String blockId) {
        return BlockType.getAssetMap().getAsset(blockId);
    }

    /**
     * Get the integer ID for a block by its string ID
     *
     * @param blockId
     * @return
     */
    public static int getBlockId(@Nonnull String blockId) {
        return BlockType.getAssetMap().getIndex(blockId);
    }
}
