package dev.twunk.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.system.response.TickContinue;
import dev.twunk.system.response.TickResponse;
import dev.twunk.system.response.TickSleep;
import dev.twunk.system.response.TickStop;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO make a heap kinda thing with constant time access by assigning
// and freeing IDs for for entities. why? i forget, knew i needed something
// like that ages ago

// my guy that's responsible for keeping track of entity states, well,
// more so just responsible for keeping the ticking ones in a nice easy
// to loop bucket
//
// OH and responsible for finding the block info when they're loaded (coords,
// the world and chunk its in, etc)
public class TrackedEntities {

    // A unique and STABLE identifier for the system. you cannot change this.
    // once you decide on an ID your players REQUIRE it to be stable (or everything
    // in their worlds will break)
    //
    // This is what is used for entities to "remember" if they're sleeping, ticking etc
    // in your system when they're loaded back in.
    //
    // hence, if you change this ID all entities will still store but forever forget
    // what state they held
    private final String id;

    public TrackedEntities(final String id) {
        this.id = id;
    }

    @Nonnull
    public static final ComponentType<ChunkStore, TickSchedulerComponent> TICK_STATE_COMPONENT =
        TickSchedulerComponent.getComponentType();

    @Nonnull
    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, BlockModule.BlockStateInfo> BLOCK_INFO_COMPONENT_TYPE =
        BlockModule.BlockStateInfo.getComponentType();

    @Nonnull
    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT_TYPE =
        WorldChunk.getComponentType();

    // \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/
    // Non-static implementation begins
    // \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/

    @Nonnull
    public final ArrayList<TrackedBlockEntity> ticking = new ArrayList<>();

    @Nonnull
    private final ArrayList<TrackedBlockEntity> sleeping = new ArrayList<>();

    @Nonnull
    private final ArrayList<TrackedBlockEntity> comatose = new ArrayList<>();

    @Nonnull
    private final ArrayList<TrackedBlockEntity> stopped = new ArrayList<>();

    @Nonnull
    private final ArrayList<TrackedBlockEntity> broken = new ArrayList<>();

    public void track(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final Store<ChunkStore> store,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer
    ) {
        // figure out the current/initial ticking state our entity has
        var tickingInfo = this.loadEntityTickingState(ref, commandBuffer);
        var initialState = tickingInfo.getTickingInfo(this.id);

        // Figure out which tick group we should put our entity in based on its
        // state
        var area = this.getOwner(initialState);

        // prepare the variables/references we need to run our tick method
        // (whenever that tick happens)
        var onTickCache = TrackedEntities.getTickVars(ref, store, area);
        if (onTickCache == null) {
            return;
        }

        // we'll put chuck our cache into the right ticking group (ready to go)
        area.add(onTickCache);

        // and finally, we'll write down the area that we put said cache
        // so our component (found by ref) can remove itself easily
        // when it gets removed
        tickingInfo._setMemoryLocation(this.id, onTickCache);
    }

    public void untrack(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final Store<ChunkStore> store,
        @Nonnull final RemoveReason reason
    ) {
        store.getComponent(ref, TrackedEntities.TICK_STATE_COMPONENT).drop(this.id, reason);
    }

    /**
     * Get a cached version of the info required to tick an entity
     */
    @Nullable
    private static TrackedBlockEntity getTickVars(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final Store<ChunkStore> store,
        @Nonnull final ArrayList<TrackedBlockEntity> area
    ) {
        // We're going to spend a bunch of extra time in onEntityAdd to cache
        // all the information we'll need when this thing is ticking
        //
        // Most of this starts from the "info"
        var info = store.getComponent(ref, BLOCK_INFO_COMPONENT_TYPE);
        if (info == null) {
            return null;
        }

        // Now, we use the info to get the chunk that we're in
        var chunkRef = info.getChunkRef();
        var chunk = store.getComponent(chunkRef, WORLD_CHUNK_COMPONENT_TYPE);
        if (chunk == null) {
            return null;
        }
        var world = chunk.getWorld();
        if (world == null) {
            return null;
        }

        // We use the info + chunk to get the global coords of the block
        // using some magic I found in the depths of the hytale source code
        // split across a couple files
        var indexInChunk = info.getIndex();
        var globalCoords = new Vector3i(
            (chunk.getX() << 5) | (indexInChunk & 31),
            (indexInChunk >> 10) & ChunkUtil.HEIGHT_MASK,
            (chunk.getZ() << 5) | ((indexInChunk >> 5) & 31)
        );

        // lets get this all bundled up for easy re-use
        var blockId = chunk.getBlock(globalCoords);
        var cache = new TrackedBlockEntity(world, chunk, ref, globalCoords, blockId, area);

        return cache;
    }

    @Nonnull
    private TickSchedulerComponent loadEntityTickingState(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Setup a tickingInfo component to track the state of our entitiy
        // so it can resume ticking/sleeping/etc when the server reboots. really
        // we just want to store shit so the lifetime extends past (NOW), and
        // so we can QUICKLY remove the entity again later
        var tickingInfo = commandBuffer.ensureAndGetComponent(ref, TICK_STATE_COMPONENT);
        var systemState = tickingInfo.getTickingInfo(this.id);
        if (systemState == null) {
            systemState = new TickContinue();
            tickingInfo.setTickingInfo(this.id, systemState);
        }

        return tickingInfo;
    }

    /**
     * Get a ref to the list in which this entity should be placed based on its
     * current ticking state (active, sleeping, stopped etc)
     * @return
     */
    @Nonnull
    private ArrayList<TrackedBlockEntity> getOwner(TickResponse currentState) {
        // and finally, we'll store it in the right place
        if (currentState instanceof TickContinue) {
            return ticking;
        } else if (currentState instanceof TickSleep) {
            if (((TickSleep) currentState).isIndefinite()) {
                return comatose;
            } else {
                return sleeping;
            }
        } else if (currentState instanceof TickStop) {
            return stopped;
        } else {
            return broken;
        }
    }
}
