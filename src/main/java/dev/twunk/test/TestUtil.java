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
    public final BlockComponentChunk blockChunk;

    @SuppressWarnings("unused")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public TestUtil(
        @Nonnull final Ref<ChunkStore> blockRef,
        @Nonnull final WorldChunk worldChunk,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull final Vector3i blockCoords
    ) {
        this.commandBuffer = commandBuffer;

        this.store = commandBuffer.getStore(); //  this works too
        // this.store = blockRef.getStore();

        this.chunkStore = commandBuffer.getExternalData(); // this one works too
        // this.chunkStore = blockRef.getStore().getExternalData();

        this.worldProvider = this.chunkStore;

        this.world = commandBuffer.getExternalData().getWorld();
        // this.world = blockRef.getStore().getExternalData().getWorld(); // this works too

        this.worldChunk = worldChunk;

        var blockChunk = worldChunk.getBlockComponentChunk();
        if (blockChunk == null) {
            throw new RuntimeException("ERROR: BlockChunk was null!!!");
        }

        this.blockChunk = blockChunk;

        this.blockRef = blockRef;
        // this.blockRef = Utils.Block.Ref_.getRef(commandBuffer, blockCoords); // this works too

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
        var wlrdChunk = Utils.Chunk.WorldChunk_.getWorldChunk(info);
        if (wlrdChunk == null) {
            throw new RuntimeException("ERROR: wlrdChunk was null");
        }


        /** this works too */
        if (Utils.Chunk.WorldChunk_.getWorldChunkFromBlock(blockRef) == null) {
            throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromBlock(blockRef) was null");
        }

        // sick, stuff seems to be working now?? weird
        if (Utils.Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef) == null) {
            throw new RuntimeException("ERROR: Utils.Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef) was null");
        }
        this.info = info;
    }
}
