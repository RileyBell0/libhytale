package dev.twunk.lib.test;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public final class TestUtil {

    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_INFO_COMPONENT =
        BlockStateInfo.getComponentType();

    // @SuppressWarnings("null")
    // private static final ComponentType<ChunkStore, BlockChunk> BLOCK_CHUNK_COMPONENT = BlockChunk.getComponentType();

    public final WorldProvider worldProvider;

    public final World world;

    public final CommandBuffer<ChunkStore> commandBuffer;

    public final BlockStateInfo info;

    public final Ref<ChunkStore> chunkRef;

    public final Store<ChunkStore> store;

    public final ChunkStore chunkStore;

    public final Ref<ChunkStore> blockRef;

    public final WorldChunk worldChunk;

    public final BlockComponentChunk blockComponentChunk;

    public final BlockChunk blockChunk;

    // public TestUtil(
    //     final WorldChunk worldChunk,
    //     final CommandBuffer<ChunkStore> commandBuffer,
    //     final Vector3i blockCoords
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
    public TestUtil(final CommandBuffer<ChunkStore> commandBuffer, final Vector3i blockCoords) {
        this.commandBuffer = commandBuffer;

        this.store = commandBuffer.getStore(); //  this works too
        // this.store = blockRef.getStore();

        this.chunkStore = commandBuffer.getExternalData(); // this one works too
        // this.chunkStore = blockRef.getStore().getExternalData();

        this.worldProvider = this.chunkStore;

        this.world = commandBuffer.getExternalData().getWorld();
        // this.world = blockRef.getStore().getExternalData().getWorld(); // this works too

        final var worldChunk = dev.twunk.hytale.utils.ChunkUtils.WorldChunk_.get(commandBuffer, blockCoords);
        if (worldChunk == null) {
            throw new RuntimeException("ERROR: worldChunk was null!!!");
        }
        this.worldChunk = worldChunk;

        final var blockComponentChunk = worldChunk.getBlockComponentChunk();
        if (blockComponentChunk == null) {
            throw new RuntimeException("ERROR: BlockChunk was null!!!");
        }

        this.blockComponentChunk = blockComponentChunk;

        // this.blockRef = blockRef; // this works too
        final var blockRef = dev.twunk.hytale.utils.BlockUtils.Ref_.get(commandBuffer, blockCoords);
        if (blockRef == null) {
            throw new RuntimeException("ERROR: Failed to get ref for block at " + blockCoords);
        }
        this.blockRef = blockRef;

        final var chunkRef = worldChunk.getReference();
        if (chunkRef == null) {
            throw new RuntimeException("ERROR: chunk ref was null!!");
        }

        this.chunkRef = chunkRef;
        final var info = blockRef.getStore().getComponent(blockRef, BLOCK_INFO_COMPONENT);
        if (info == null) {
            throw new RuntimeException("ERROR: info was null");
        }

        // this works
        final var wlrdChunk = dev.twunk.hytale.utils.ChunkUtils.WorldChunk_.get(info);
        if (wlrdChunk == null) {
            throw new RuntimeException("ERROR: wlrdChunk was null");
        }

        final var blockChunk = this.worldChunk.getBlockChunk();
        if (blockChunk == null) {
            throw new RuntimeException("ERROR: blockChunk was null");
        }
        this.blockChunk = blockChunk;

        /** this works too */
        if (dev.twunk.hytale.utils.ChunkUtils.WorldChunk_.get_blockRef(blockRef) == null) {
            throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromBlock(blockRef) was null");
        }

        // sick, stuff seems to be working now?? weird
        if (dev.twunk.hytale.utils.ChunkUtils.WorldChunk_.get_chunkRef(chunkRef) == null) {
            throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef) was null");
        }
        this.info = info;
    }
}
