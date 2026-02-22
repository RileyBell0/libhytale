package dev.twunk.utils.world;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.utils.world.Utils.Coords;
import javax.annotation.Nonnull;

public final class TestUtil {

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

    @SuppressWarnings("null")
    public TestUtil(@Nonnull final CommandBuffer<ChunkStore> commandBuffer, @Nonnull final Vector3i blockCoods) {
        this.commandBuffer = commandBuffer;
        this.store = commandBuffer.getStore();
        this.chunkStore = commandBuffer.getExternalData();
        this.world = commandBuffer.getExternalData().getWorld();
        this.worldProvider = (WorldProvider) world;
        this.worldChunk = world.getChunk(Coords.getChunkIndex(blockCoods));
        this.blockChunk = worldChunk.getBlockComponentChunk();
        this.blockRef = worldChunk.getBlockComponentEntity(blockCoods.x, blockCoods.y, blockCoods.z);
        this.chunkRef = worldChunk.getReference();
        this.info = blockRef.getStore().getComponent(blockRef, BlockStateInfo.getComponentType());
    }
}
