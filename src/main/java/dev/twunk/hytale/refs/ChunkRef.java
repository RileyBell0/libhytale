package dev.twunk.hytale.refs;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
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
public class ChunkRef extends Ref<ChunkStore> {

    @Nonnull
    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT_TYPE =
        WorldChunk.getComponentType();

    @Nullable
    private Long chunkIndex = null;

    @Nullable
    private World world = null;

    @Nullable
    private WorldChunk worldChunk = null;

    public ChunkRef(@Nonnull Ref<ChunkStore> ref) {
        super(ref.getStore());
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
        var chunkIndex = this.getChunkIndex();
        if (chunkIndex == null) {
            return null;
        }

        final int chunkX = (int) (chunkIndex >> 32);
        final int chunkZ = (int) (long) (chunkIndex);

        return new ChunkCoordinates(chunkX, chunkZ);
    }

    @Nullable
    public ChunkRef getOtherChunkRef(long otherChunkIndex) {
        // skip looking up other chunks if its the same one
        if (otherChunkIndex == this.getChunkIndex()) {
            return this;
        }

        var otherRef = ChunkUtils.Ref_.get(this, otherChunkIndex);
        if (otherRef == null) {
            return null;
        }

        return new ChunkRef(otherRef);
    }

    // suppressing "unchecked" but, really, i've checked it. silly java.
    @SuppressWarnings("unchecked")
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

        this.worldChunk = ChunkUtils.WorldChunk_.get_chunkRef(this);

        return this.worldChunk;
    }

    @Nonnull
    public World getWorld() {
        if (this.world != null) {
            return this.world;
        }

        this.world = this.getStore().getExternalData().getWorld();

        return this.world;
    }

    @Nonnull
    @Override
    public String toString() {
        String var10000 = String.valueOf(this.getStore().getClass());
        return (
            "ChunkRef{Ref{store=" + var10000 + "@" + this.getStore().hashCode() + ", index=" + this.getIndex() + "}}"
        );
    }
}
