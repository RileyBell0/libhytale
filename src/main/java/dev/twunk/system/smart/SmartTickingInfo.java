package dev.twunk.system.smart;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.IRegisteredComponent;
import dev.twunk.system.response.TickResponse;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Singleton-like component
 * - NOT one SmartTickInfo per server
 * - YES one SmartTickInfo per ENTITY
 *
 * basically, this is an extension of my SmartTickSystem that i shove onto your
 * entities when you're using SmartTickSystem
 *
 * no touchy. this won't be a big memory footprint unless you're doing stupid
 * stuff
 *
 * but to be fair, stupid stuff is fun stuff
 */
public class SmartTickingInfo implements IRegisteredComponent {

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<SmartTickingInfo> CODEC = BuilderCodec.builder(
        SmartTickingInfo.class,
        SmartTickingInfo::new
    ).build();

    @SuppressWarnings("null")
    @Nonnull
    public static ComponentType<ChunkStore, SmartTickingInfo> COMPONENT_TYPE;

    /**
     * The idea is to only have 1x of this component per entity, thus, since my
     * smart ticking system supports queries and as many smart ticking systems as
     * you want, we need to make sure that basically this is a SINGLETON per entity
     *
     * why not singleton globally? cause then i have to store and load a ton of
     * info.
     *
     * imagine you've loaded 1tb of chunks. now imagine i need to load that all at
     * once into memory. yeah, we don't want that. sure it'd be faster with my
     * testing for now, but it wouldn't scale
     *
     * and god knows, block games really have alot of blocks
     */
    @Nonnull
    private final Int2ObjectOpenHashMap<TickResponse> tickingState = new Int2ObjectOpenHashMap<>();

    /**
     * Map from SystemID to locations where the item is stored (memory only,
     * not stored to disk)
     */
    @Nonnull
    private final Int2ObjectOpenHashMap<TickingEntityMetadata> memoryLocation = new Int2ObjectOpenHashMap<>();

    /**
     * Store the current ticking state we've got for the given system (e.g. awake,
     * sleeping, etc)
     *
     * Returns the previously set state (if one was already there)
     */
    @Nullable
    public TickResponse setTickingInfo(int systemId, @Nonnull TickResponse state) {
        return this.tickingState.put(systemId, state);
    }

    /**
     * Get the current state of our component for the given system (e.g. i'm
     * currently awake and ticking, or asleep until ___ etc etc)
     */
    @Nullable
    public TickResponse getTickingInfo(int systemId) {
        return this.tickingState.get(systemId);
    }

    /**
     * Internal function for me. No touchy.
     *
     * Stores a ref to the place i currently keep track of our ticking state for our
     * object
     *
     * useful so when the block that contains <THIS> is removed, i can do cleanup
     * and *not* tick it anymore
     */
    @Nullable
    public TickingEntityMetadata _setMemoryLocation(int systemId, @Nonnull TickingEntityMetadata state) {
        return memoryLocation.put(systemId, state);
    }

    /**
     * Track a ref to where (in SmartTickSystem) i currently keep the ref to
     * <THIS>
     *
     * useful so when the block that contains <THIS> is removed, i can do cleanup
     * and *not* tick it anymore
     */
    @Nullable
    public TickingEntityMetadata _getMemoryLocation(int systemId) {
        return memoryLocation.get(systemId);
    }

    /**
     * Track a ref to where (in SmartTickSystem) i currently keep the ref to
     * <THIS>
     *
     * useful so when the block that contains <THIS> is removed, i can do cleanup
     * and *not* tick it anymore
     */
    @Nullable
    public TickingEntityMetadata _dumpMemoryLocation(int systemId, RemoveReason reason) {
        if (reason == RemoveReason.REMOVE) {
            this.tickingState.remove(systemId);
        }
        return memoryLocation.remove(systemId);
    }

    public void drop(int systemId, @Nonnull RemoveReason reason) {
        if (reason == RemoveReason.REMOVE) {
            this.tickingState.remove(systemId);
        }
        var cache = memoryLocation.remove(systemId);
        cache.drop();
    }

    @Nonnull
    public SmartTickingInfo clone() {
        return new SmartTickingInfo();
    }

    @Nonnull
    public static ComponentType<ChunkStore, SmartTickingInfo> getComponentType() {
        return (ComponentType<ChunkStore, SmartTickingInfo>) IRegisteredComponent.getComponentType(
            SmartTickingInfo.class
        );
    }
}
