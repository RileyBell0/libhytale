package dev.twunk.hytale.refs;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.utils.BlockUtils;
import dev.twunk.hytale.utils.ChunkUtils;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.lib.coords.ChunkCoordinates;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements the SAME methods as Ref<ChunkStore> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 */
public class BlockRef extends Ref<ChunkStore> {

    @Nonnull
    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT_TYPE =
        BlockStateInfo.getComponentType();

    @Nullable
    private Integer blockIndex = null;

    @Nullable
    private Long chunkIndex = null;

    @Nullable
    private ChunkRef chunkRef = null;

    @Nullable
    private Vector3i blockCoords = null;

    @Nullable
    private BlockStateInfo info = null;

    private BlockRef(@Nonnull Ref<ChunkStore> ref) {
        super(ref.getStore());
    }

    // suppressing "unchecked" but, really, i've checked it. silly java.
    @SuppressWarnings("unchecked")
    public <T extends Component<ChunkStore>> T getComponent(@Nullable ComponentType<ChunkStore, T> componentType) {
        if (componentType == null) {
            return null;
        }

        // just gonna cache BlockStateInfo in here if i see it, i assume thats a
        // frequent enough slip up that i'd prefer to just cache `info` rather
        // than keep computing it
        if (componentType == BLOCK_STATE_INFO_COMPONENT_TYPE) {
            return (T) this.getInfo();
        }

        return ComponentUtils.get(this, componentType);
    }

    @Nullable
    public BlockStateInfo getInfo() {
        if (this.info != null) {
            return this.info;
        }

        this.info = BlockUtils.Info.get(this);

        return this.info;
    }

    @Nullable
    public Integer getBlockIndex() {
        if (this.blockIndex != null) {
            return this.blockIndex;
        }
        this.blockIndex = BlockUtils.Coords.Index.get(this);

        return this.blockIndex;
    }

    @Nullable
    public ChunkRef getOtherChunkRef(long otherChunkIndex) {
        var chunkRef = this.getChunkRef();
        if (chunkRef == null) {
            return null;
        }

        return chunkRef.getOtherChunkRef(otherChunkIndex);
    }

    @Nullable
    public Integer getBlockId() {
        // block ID can only be found via world chunk
        var chunkRef = this.getChunkRef();
        if (chunkRef == null) {
            return null;
        }

        var worldChunk = chunkRef.getWorldChunk();
        var blockCoords = this.getBlockCoords();
        if (worldChunk == null || blockCoords == null) {
            return null;
        }

        return worldChunk.getBlock(blockCoords);
    }

    @Nullable
    public Integer getOtherBlockId(@Nonnull Vector3i otherBlockCoords) {
        // block ID can only be found via world chunk
        var chunkRef = this.getChunkRef();
        if (chunkRef == null) {
            return null;
        }

        var worldChunk = chunkRef.getWorldChunk();
        if (worldChunk == null) {
            return null;
        }

        return worldChunk.getBlock(otherBlockCoords);
    }

    @Nullable
    public Vector3i getBlockCoords() {
        if (this.blockCoords != null) {
            return this.blockCoords;
        }

        var chunkIndex = this.getChunkIndex();
        var blockIndex = this.getBlockIndex();
        if (chunkIndex == null || blockIndex == null) {
            return null;
        }

        this.blockCoords = BlockUtils.Coords.Global.get(chunkIndex, blockIndex);

        return this.blockCoords;
    }

    @Nullable
    public Long getChunkIndex() {
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
    public ChunkRef getChunkRef() {
        if (this.chunkRef != null) {
            return this.chunkRef;
        }
        var ref = ChunkUtils.Ref_.get(this);
        if (ref == null) {
            return null;
        }

        this.chunkRef = new ChunkRef(ref);

        return this.chunkRef;
    }

    @Nullable
    public ChunkCoordinates getChunkCoords() {
        var chunkIndex = this.getChunkIndex();
        if (chunkIndex == null) {
            return null;
        }
        final int chunkX = (int) (chunkIndex >> 32);
        final int chunkZ = (int) (long) (chunkIndex);

        return new ChunkCoordinates(chunkX, chunkZ);
    }

    @Nonnull
    @Override
    public String toString() {
        String var10000 = String.valueOf(this.getStore().getClass());
        return (
            "BlockRef{Ref{store=" + var10000 + "@" + this.getStore().hashCode() + ", index=" + this.getIndex() + "}}"
        );
    }
}
