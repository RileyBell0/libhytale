package com.example.plugin.utils;

import com.example.plugin.structs.ExampleBlock;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public static Vector3i getCoordsInChunk(@Nonnull BlockModule.BlockStateInfo info) {
        var indexInChunk = info.getIndex();
        int x = ChunkUtil.xFromBlockInColumn(indexInChunk);
        int y = ChunkUtil.yFromBlockInColumn(indexInChunk);
        int z = ChunkUtil.zFromBlockInColumn(indexInChunk);

        return new Vector3i(x, y, z);
    }

    // Get the local coords of the block in its chunk
    public static Vector3i getCoordsInChunk(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        var info = BlockUtils.getInfo(commandBuffer, ref);
        if (info == null) {
            return null;
        }

        return BlockUtils.getCoordsInChunk(info);
    }

    public static CompletableFuture<Ref<ChunkStore>> get(World world, int x, int y, int z) {
        var worldChunk = world.getChunkAsync(ChunkUtil.indexChunkFromBlock(x, z));
        var blockRef = worldChunk.thenApply(chunk -> chunk.getBlockComponentEntity(x, y, z));

        return blockRef;
    }

    public static CompletableFuture<Ref<ChunkStore>[]> getTouching(World world, int x, int y, int z) {
        var future = new CompletableFuture<Ref<ChunkStore>[]>();

        @SuppressWarnings("unchecked")
        Ref<ChunkStore>[] items = new Ref[6];

        var block0 = BlockUtils.get(world, x, y, z + 1);
        var block1 = BlockUtils.get(world, x, y, z - 1);
        var block2 = BlockUtils.get(world, x, y + 1, z);
        var block3 = BlockUtils.get(world, x, y - 1, z);
        var block4 = BlockUtils.get(world, x + 1, y, z);
        var block5 = BlockUtils.get(world, x - 1, y, z);
        CompletableFuture.allOf(block0, block1, block2, block3, block4, block5).thenRun(() -> {
            items[0] = block0.join();
            items[1] = block1.join();
            items[2] = block2.join();
            items[3] = block3.join();
            items[4] = block4.join();
            items[5] = block5.join();

            future.complete(items);
        });

        return future;
    }

    public static boolean setTicking(@Nonnull CommandBuffer<ChunkStore> cmd, @Nonnull Ref<ChunkStore> ref) {
        return BlockUtils.setTicking(cmd, ref, true);
    }

    public static boolean setTicking(
        @Nonnull CommandBuffer<ChunkStore> cmd,
        @Nonnull Ref<ChunkStore> ref,
        boolean ticking
    ) {
        var info = BlockUtils.getInfo(cmd, ref);
        if (info == null) {
            console.log("Info was null");
            return false;
        }

        return BlockUtils.setTicking(cmd, info, ticking);
    }

    public static boolean setTicking(@Nonnull CommandBuffer<ChunkStore> cmd, @Nonnull BlockModule.BlockStateInfo info) {
        return BlockUtils.setTicking(cmd, info, true);
    }

    public static boolean setTicking(
        @Nonnull CommandBuffer<ChunkStore> cmd,
        @Nonnull BlockModule.BlockStateInfo info,
        boolean ticking
    ) {
        var worldChunk = BlockUtils.getWorldChunk(cmd, info);
        if (worldChunk == null) {
            console.log("World chunk was null");
            return false;
        }

        var coords = BlockUtils.getCoordsInChunk(info);
        return BlockUtils.setTicking(worldChunk, coords, ticking);
    }

    public static boolean setTicking(@Nonnull WorldChunk worldChunk, @Nonnull Vector3i coords) {
        return BlockUtils.setTicking(worldChunk, coords, true);
    }

    public static boolean setTicking(@Nonnull WorldChunk worldChunk, @Nonnull Vector3i coords, boolean ticking) {
        return worldChunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }

    @FunctionalInterface
    public interface BlockIGuess<T extends Component<ChunkStore>> {
        ComponentType<ChunkStore, T> getComponentType();
    }

    public static <T extends Component<ChunkStore>> boolean hasComponent(
        @Nonnull CommandBuffer<ChunkStore> cmd,
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull BlockIGuess<T> t
    ) {
        return (ExampleBlock) cmd.getComponent(ref, ExampleBlock.getComponentType()) != null;
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
