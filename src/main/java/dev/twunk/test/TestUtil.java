package dev.twunk.test;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.utils.world.Utils;
import javax.annotation.Nonnull;

public final class TestUtil {

    @SuppressWarnings("null")
    @Nonnull
    private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_INFO_COMPONENT =
        BlockStateInfo.getComponentType();

    // @SuppressWarnings("null")
    // @Nonnull
    // private static final ComponentType<ChunkStore, BlockChunk> BLOCK_CHUNK_COMPONENT = BlockChunk.getComponentType();

    @Nonnull
    public final WorldProvider worldProvider;

    @Nonnull
    public final World world;

    @Nonnull
    public final CommandBuffer<ChunkStore> commandBuffer;

    @Nonnull
    public final BlockStateInfo info;

    @Nonnull
    public final Ref<ChunkStore> chunkRef;

    @Nonnull
    public final Store<ChunkStore> store;

    @Nonnull
    public final ChunkStore chunkStore;

    @Nonnull
    public final Ref<ChunkStore> blockRef;

    @Nonnull
    public final WorldChunk worldChunk;

    @Nonnull
    public final BlockComponentChunk blockComponentChunk;

    @Nonnull
    public final BlockChunk blockChunk;

    @SuppressWarnings("unused")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    // public TestUtil(
    //     @Nonnull final WorldChunk worldChunk,
    //     @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
    //     @Nonnull final Vector3i blockCoords
    // ) {
    //     this.commandBuffer = commandBuffer;

    //     this.store = commandBuffer.getStore(); //  this works too
    //     // this.store = blockRef.getStore();

    //     this.chunkStore = commandBuffer.getExternalData(); // this one works too
    //     // this.chunkStore = blockRef.getStore().getExternalData();

    //     this.worldProvider = this.chunkStore;

    //     this.world = commandBuffer.getExternalData().getWorld();
    //     // this.world = blockRef.getStore().getExternalData().getWorld(); // this works too

    //     this.worldChunk = worldChunk;

    //     var blockChunk = worldChunk.getBlockComponentChunk();
    //     if (blockChunk == null) {
    //         throw new RuntimeException("ERROR: BlockChunk was null!!!");
    //     }

    //     this.blockChunk = blockChunk;

    //     // this.blockRef = blockRef; // this works too
    //     var blockRef = Utils.Block.Ref_.getRef(commandBuffer, blockCoords);
    //     if (blockRef == null) {
    //         throw new RuntimeException("ERROR: Failed to get ref for block at " + blockCoords);
    //     }
    //     this.blockRef = blockRef;

    //     var chunkRef = worldChunk.getReference();
    //     if (chunkRef == null) {
    //         throw new RuntimeException("ERROR: chunk ref was null!!");
    //     }

    //     this.chunkRef = chunkRef;
    //     var info = blockRef.getStore().getComponent(blockRef, BLOCK_INFO_COMPONENT);
    //     if (info == null) {
    //         throw new RuntimeException("ERROR: info was null");
    //     }

    //     // this works
    //     var wlrdChunk = Utils.Chunk.WorldChunk_.getWorldChunk(info);
    //     if (wlrdChunk == null) {
    //         throw new RuntimeException("ERROR: wlrdChunk was null");
    //     }

    //     /** this works too */
    //     if (Utils.Chunk.WorldChunk_.getWorldChunkFromBlock(blockRef) == null) {
    //         throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromBlock(blockRef) was null");
    //     }

    //     // sick, stuff seems to be working now?? weird
    //     if (Utils.Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef) == null) {
    //         throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef) was null");
    //     }
    //     this.info = info;
    // }

    /**
     * All methods that work with command buffer ALSO work with a block ref :) WOOOOOO
     * @param commandBuffer
     * @param blockCoords
     */
    public TestUtil(@Nonnull final CommandBuffer<ChunkStore> commandBuffer, @Nonnull final Vector3i blockCoords) {
        this.commandBuffer = commandBuffer;

        this.store = commandBuffer.getStore(); //  this works too
        // this.store = blockRef.getStore();

        this.chunkStore = commandBuffer.getExternalData(); // this one works too
        // this.chunkStore = blockRef.getStore().getExternalData();

        this.worldProvider = this.chunkStore;

        this.world = commandBuffer.getExternalData().getWorld();
        // this.world = blockRef.getStore().getExternalData().getWorld(); // this works too

        var worldChunk = Utils.Chunk.WorldChunk_.get(commandBuffer, blockCoords);
        if (worldChunk == null) {
            throw new RuntimeException("ERROR: worldChunk was null!!!");
        }
        this.worldChunk = worldChunk;

        var blockComponentChunk = worldChunk.getBlockComponentChunk();
        if (blockComponentChunk == null) {
            throw new RuntimeException("ERROR: BlockChunk was null!!!");
        }

        this.blockComponentChunk = blockComponentChunk;

        // this.blockRef = blockRef; // this works too
        var blockRef = Utils.Block.Ref_.get(commandBuffer, blockCoords);
        if (blockRef == null) {
            throw new RuntimeException("ERROR: Failed to get ref for block at " + blockCoords);
        }
        this.blockRef = blockRef;

        var chunkRef = worldChunk.getReference();
        if (chunkRef == null) {
            throw new RuntimeException("ERROR: chunk ref was null!!");
        }

        this.chunkRef = chunkRef;
        var info = blockRef.getStore().getComponent(blockRef, BLOCK_INFO_COMPONENT);
        if (info == null) {
            throw new RuntimeException("ERROR: info was null");
        }

        // this works
        var wlrdChunk = Utils.Chunk.WorldChunk_.get(info);
        if (wlrdChunk == null) {
            throw new RuntimeException("ERROR: wlrdChunk was null");
        }

        var blockChunk = this.worldChunk.getBlockChunk();
        if (blockChunk == null) {
            throw new RuntimeException("ERROR: blockChunk was null");
        }
        this.blockChunk = blockChunk;

        /** this works too */
        if (Utils.Chunk.WorldChunk_.get_blockRef(blockRef) == null) {
            throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromBlock(blockRef) was null");
        }

        // sick, stuff seems to be working now?? weird
        if (Utils.Chunk.WorldChunk_.get_chunkRef(chunkRef) == null) {
            throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef) was null");
        }
        this.info = info;
    }
}
