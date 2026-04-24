package dev.twunk.hytale.ref;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.types.ChunkCoordinates;
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
public class ChunkRef extends AnyRef<ChunkStore> {

    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT_TYPE =
        WorldChunk.getComponentType();

    @Nullable
    private Long chunkIndex = null;

    @Nullable
    private WorldChunk worldChunk = null;

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    public ChunkRef(Ref<ChunkStore> ref) {
        super(ref);
    }

    @Nullable
    public Long getChunkIndex() {
        if (this.chunkIndex != null) {
            return this.chunkIndex;
        }

        this.chunkIndex = ChunkUtils.Coords.Index.get(this);

        return this.chunkIndex;
    }

    @Nullable
    public ChunkCoordinates getChunkCoords() {
        var loadedChunkIndex = this.getChunkIndex();
        if (loadedChunkIndex == null) {
            return null;
        }

        final int chunkX = (int) (loadedChunkIndex >> 32);
        final int chunkZ = (int) (long) (loadedChunkIndex);

        return new ChunkCoordinates(chunkX, chunkZ);
    }

    @Nullable
    public ChunkRef getOtherChunkRef(long otherChunkIndex) {
        // skip looking up other chunks if its the same one
        if (otherChunkIndex == this.getChunkIndex()) {
            return this;
        }

        var otherRef = ChunkUtils.Refs.get(this, otherChunkIndex);
        if (otherRef == null) {
            return null;
        }

        return new ChunkRef(otherRef);
    }

    // suppressing "unchecked" but, really, i've checked it. silly java.
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends Component<ChunkStore>> T getComponent(@Nullable ComponentType<ChunkStore, T> componentType) {
        if (componentType == null) {
            return null;
        }

        // just gonna cache world chunk in here if i see it, i assume thats a
        // frequent enough slip up that i'd prefer to just cache `WorldChunk` rather
        // than keep computing it
        if (componentType == WORLD_CHUNK_COMPONENT_TYPE) {
            return (T) this.getWorldChunk();
        }

        return ComponentUtils.get(this, componentType);
    }

    @Nullable
    public WorldChunk getWorldChunk() {
        if (this.worldChunk != null) {
            return this.worldChunk;
        }

        this.worldChunk = ChunkUtils.WorldChunks.get_chunkRef(this);

        return this.worldChunk;
    }

    @Override
    public String toString() {
        return "ChunkRef{" + super.toString() + "}";
    }
}
