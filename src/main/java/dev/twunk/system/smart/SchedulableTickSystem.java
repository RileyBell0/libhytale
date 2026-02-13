package dev.twunk.system.smart;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.system.response.TickContinue;
import dev.twunk.system.response.TickResponse;
import dev.twunk.system.response.TickSleep;
import dev.twunk.system.response.TickStop;
import java.util.ArrayList;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SchedulableTickSystem {

    private static int nextId = 0;

    public final int id;

    @Nonnull
    private final TrackedEntities entities = new TrackedEntities();

    @Nonnull
    private final LifetimeSystem lifetimeSystem = new LifetimeSystem();

    @Nonnull
    private final TickSystem entityTickSystem = new TickSystem();

    @Nonnull
    private final Query<ChunkStore> query;

    public SchedulableTickSystem(@Nonnull Query<ChunkStore> query) {
        this.id = nextId++;
        this.query = query;
    }

    /**
     * Actual method to run when an entity actually ticks (given your
     * system/strategy/rules etc)
     *
     * @param dt             delta time
     * @param ref            ref to your entity
     * @param archetypeChunk
     * @param store
     * @param commandBuffer
     *
     * @return null for "don't change anything", or TickResponse to tell us what you
     *         want us to do
     */
    @Nullable
    public abstract TickResponse onEntityTick(
        @Nonnull TickingEntityMetadata state,
        float dt,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    );

    // my guy that's responsible for keeping track of entity states, well,
    // more so just responsible for keeping the ticking ones in a nice easy
    // to loop bucket
    //
    // OH and responsible for finding the block info when they're loaded (coords,
    // the world and chunk its in, etc)
    private class TrackedEntities {

        @Nonnull
        public static final ComponentType<ChunkStore, SmartTickingInfo> TICK_STATE_COMPONENT =
            SmartTickingInfo.getComponentType();

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
        private final ArrayList<TickingEntityMetadata> ticking = new ArrayList<>();

        @Nonnull
        private final ArrayList<TickingEntityMetadata> sleeping = new ArrayList<>();

        @Nonnull
        private final ArrayList<TickingEntityMetadata> comatose = new ArrayList<>();

        @Nonnull
        private final ArrayList<TickingEntityMetadata> stopped = new ArrayList<>();

        @Nonnull
        private final ArrayList<TickingEntityMetadata> broken = new ArrayList<>();

        public void track(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final Store<ChunkStore> store,
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer
        ) {
            // figure out the current/initial ticking state our entity has
            var tickingInfo = this.loadEntityTickingState(ref, commandBuffer);
            var initialState = tickingInfo.getTickingInfo(SchedulableTickSystem.this);

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
            tickingInfo._setMemoryLocation(SchedulableTickSystem.this, onTickCache);
        }

        public void untrack(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final Store<ChunkStore> store,
            @Nonnull final RemoveReason reason
        ) {
            store.getComponent(ref, TrackedEntities.TICK_STATE_COMPONENT).drop(SchedulableTickSystem.this, reason);
        }

        /**
         * Get a cached version of the info required to tick an entity
         */
        @Nullable
        private static TickingEntityMetadata getTickVars(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final Store<ChunkStore> store,
            @Nonnull final ArrayList<TickingEntityMetadata> area
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
            var cache = new TickingEntityMetadata(world, chunk, ref, globalCoords, blockId, area);

            return cache;
        }

        @Nonnull
        private SmartTickingInfo loadEntityTickingState(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer
        ) {
            // Setup a tickingInfo component to track the state of our entitiy
            // so it can resume ticking/sleeping/etc when the server reboots. really
            // we just want to store shit so the lifetime extends past (NOW), and
            // so we can QUICKLY remove the entity again later
            var tickingInfo = commandBuffer.ensureAndGetComponent(ref, TICK_STATE_COMPONENT);
            var systemState = tickingInfo.getTickingInfo(SchedulableTickSystem.this);
            if (systemState == null) {
                systemState = new TickContinue();
                tickingInfo.setTickingInfo(SchedulableTickSystem.this, systemState);
            }

            return tickingInfo;
        }

        /**
         * Get a ref to the list in which this entity should be placed based on its
         * current ticking state (active, sleeping, stopped etc)
         * @return
         */
        @Nonnull
        public ArrayList<TickingEntityMetadata> getOwner(TickResponse currentState) {
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

    /**
     * SUBSYSTEM
     *
     * Handles the lifetime of entities in here
     *
     * Listens for add and remove events for any entity that matches our query, then
     * communicates this to the entity ticker.
     *
     * Means that we don't have to run something for ALL entities that match our
     * query every tick, and can instead track ther life ourselves so we ONLY run
     * the entities that are ticking (with minimal mutations to components etc)
     */
    private class LifetimeSystem extends RefSystem<ChunkStore> {

        /**
         * When an entity is added to the world that matches our query, we figure out
         * if it IS ticking already (or sleeping etc), and if it DOESN'T have a state
         * for our given system, we add one, a default.
         */
        @Override
        public void onEntityAdded(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final AddReason reason,
            @Nonnull final Store<ChunkStore> store,
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer
        ) {
            entities.track(ref, store, commandBuffer);
        }

        /**
         * Mark the entity for removal.
         *
         * Entity removals from ticking, sleeping etc are all handled in the catual
         * EntityTicker class
         */
        @Override
        public void onEntityRemove(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final RemoveReason reason,
            @Nonnull final Store<ChunkStore> store,
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer
        ) {
            // drop the entity from our tracker
            entities.untrack(ref, store, reason);
        }

        /**
         * This is where the parent's query actually gets used - only for the entity
         * register
         *
         * We follow the lifecycle of refs which match our query, we only run when they
         * are added or removed, so it's pretty efficient and safe (and ideal) to run
         * over all entities that match our parents query when they get added to the
         * world or removed from it
         */
        @Override
        @Nullable
        public Query<ChunkStore> getQuery() {
            return SchedulableTickSystem.this.query;
        }
    }

    /**
     * Handles pre-tick tasks
     * - waking up sleeping entities
     * And then handles ticking tasks
     * - ticks all awake entities that match the query
     * - handles the return from the ticking entities to put them to sleep etc (if
     * need be. really just to change their state if requested)
     */
    private class TickSystem extends ArchetypeTickingSystem<ChunkStore> {

        @SuppressWarnings({ "null", "rawtypes", "unchecked" })
        @Nonnull
        private final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(
            new SystemDependency(Order.AFTER, lifetimeSystem.getClass())
        );

        /**
         * tick method that gets called by the `store`
         * this is pretty much just a shim to get into my code, as i don't want to touch
         * theirs wherever possible
         */
        @Override
        public void tick(
            float dt,
            @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer
        ) {
            // tick all our refs (ugly loop syntaxt basically just to assert that it's not
            // null. trust me, it's not, i guarantee it on its way in elsewhere, just want
            // to avoid having to check something the java type system can't pick up for
            // some reason)
            for (@Nonnull
            @SuppressWarnings("null")
            var ticker : entities.ticking) {
                // need to make it so that we check if the ref is still valid at this stage
                // (eventually)
                var res = SchedulableTickSystem.this.onEntityTick(ticker, dt, store, commandBuffer);

                // Transition to the state returned by the block
                if (res != null) {
                    switch (res.getType()) {
                        case TickResponse.TYPE_BROKEN:
                        case TickResponse.TYPE_SLEEP:
                        case TickResponse.TYPE_STOP:
                        default:
                            break;
                    }
                }
            }
        }

        @Override
        @Nullable
        public Query<ChunkStore> getQuery() {
            return SchedulableTickSystem.this.query;
        }

        // must run after the lifetime system, otherwise we'll fail to tick
        // newly added entities, and we'll tick already removed entities
        @Nonnull
        @Override
        public Set<Dependency<ChunkStore>> getDependencies() {
            return DEPENDENCIES;
        }
    }

    public void registerTo(ModPlugin plugin) {
        plugin.getChunkStoreRegistry().registerSystem(lifetimeSystem);
        plugin.getChunkStoreRegistry().registerSystem(entityTickSystem);
    }
}
