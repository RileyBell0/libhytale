package dev.twunk.hytale.ref;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.types.ChunkCoordinates;
import dev.twunk.hytale.utils.BlockUtils;
import dev.twunk.hytale.utils.ChunkUtils;
import dev.twunk.hytale.utils.ComponentUtils;
import javax.annotation.Nullable;

/**
 * Implements the SAME methods as Ref<ChunkStore> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 * @see AnyRef
 * @see Ref (Ref<ChunkStore>)
 */
public final class BlockRef extends AnyRef<ChunkStore> {

    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT_TYPE =
        BlockStateInfo.getComponentType();

    /** Index of the block within its chunk (local coords) */
    @Nullable
    private Integer blockIndex = null;

    /** Index of the chunk within its world */
    @Nullable
    private Long chunkIndex = null;

    /** Reference to the chunk containing this block */
    @Nullable
    private ChunkRef chunkRef = null;

    /** Global coordinates of the block */
    @Nullable
    private Vector3i blockCoords = null;

    /**
     * BlockStateInfo is great for getting coordinates and access to the chunk.
     * Cached because it's a really common thing to fetch for a block (in my experience
     * thus far)
     */
    @Nullable
    private BlockStateInfo info = null;

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    public BlockRef(Ref<ChunkStore> ref) {
        super(ref);
    }

    // suppressing "unchecked" but, really, i've checked it. silly java.
    @Nullable
    @Override
    public final <T extends Component<ChunkStore>> T getComponent(
        @Nullable ComponentType<ChunkStore, T> componentType
    ) {
        if (componentType == null) {
            return null;
        }

        // just gonna cache BlockStateInfo in here if i see it, i assume thats a
        // frequent enough slip up that i'd prefer to just cache `info` rather
        // than keep computing it
        if (componentType == BLOCK_STATE_INFO_COMPONENT_TYPE) {
            @SuppressWarnings("unchecked")
            var res = (T) this.getInfo();

            return res;
        }

        return ComponentUtils.get(this, componentType);
    }

    @Nullable
    public final BlockStateInfo getInfo() {
        if (this.info != null) {
            return this.info;
        }

        this.info = BlockUtils.Info.get(this);

        return this.info;
    }

    @Nullable
    public final ChunkRef getOtherChunkRef(long otherChunkIndex) {
        var loadedChunkRef = this.getChunkRef();
        if (loadedChunkRef == null) {
            return null;
        }

        return loadedChunkRef.getOtherChunkRef(otherChunkIndex);
    }

    @Nullable
    public final Integer getBlockId() {
        // block ID can only be found via world chunk
        var loadedChunkRef = this.getChunkRef();
        if (loadedChunkRef == null) {
            return null;
        }

        var worldChunk = loadedChunkRef.getWorldChunk();
        var loadedBlockCoords = this.getCoords();
        if (worldChunk == null || loadedBlockCoords == null) {
            return null;
        }

        return worldChunk.getBlock(loadedBlockCoords);
    }

    @Nullable
    public final Integer getOtherBlockId(Vector3i otherBlockCoords) {
        // block ID can only be found via world chunk
        var loadedChunkRef = this.getChunkRef();
        if (loadedChunkRef == null) {
            return null;
        }

        var worldChunk = loadedChunkRef.getWorldChunk();
        if (worldChunk == null) {
            return null;
        }

        return worldChunk.getBlock(otherBlockCoords);
    }

    @Nullable
    public final Integer getLocalIndex() {
        return this.getBlockIndex();
    }

    @Nullable
    public final Integer getBlockIndex() {
        if (this.blockIndex != null) {
            return this.blockIndex;
        }
        this.blockIndex = BlockUtils.Coords.Index.get(this);

        return this.blockIndex;
    }

    @Nullable
    public final Vector3i getCoords() {
        return this.getBlockCoords();
    }

    @Nullable
    public final Vector3i getBlockCoords() {
        if (this.blockCoords != null) {
            return this.blockCoords;
        }

        var loadedChunkIndex = this.getChunkIndex();
        var loadedBlockIndex = this.getBlockIndex();
        if (loadedChunkIndex == null || loadedBlockIndex == null) {
            return null;
        }

        this.blockCoords = BlockUtils.Coords.Global.get(loadedChunkIndex, loadedBlockIndex);

        return this.blockCoords;
    }

    @Nullable
    public final Long getChunkIndex() {
        if (this.chunkIndex != null) {
            return this.chunkIndex;
        }

        if (this.chunkRef != null) {
            this.chunkIndex = ChunkUtils.Coords.Index.get(this.chunkRef);
        } else {
            this.chunkIndex = ChunkUtils.Coords.Index.get_blockRef(this);
        }

        return this.chunkIndex;
    }

    @Nullable
    public final ChunkRef getChunkRef() {
        if (this.chunkRef != null) {
            return this.chunkRef;
        }
        var ref = ChunkUtils.Refs.get(this);
        if (ref == null) {
            return null;
        }

        this.chunkRef = new ChunkRef(ref);

        return this.chunkRef;
    }

    @Nullable
    public final ChunkCoordinates getChunkCoords() {
        var loadedChunkIndex = this.getChunkIndex();
        if (loadedChunkIndex == null) {
            return null;
        }
        final int chunkX = (int) (loadedChunkIndex >> 32);
        final int chunkZ = (int) (long) (loadedChunkIndex);

        return new ChunkCoordinates(chunkX, chunkZ);
    }

    @Override
    public final String toString() {
        return "BlockRef{" + super.toString() + "}";
    }
}
