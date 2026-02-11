package dev.twunk.ticking.system.smart;

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
import dev.twunk.interfaces.ModPlugin;
import dev.twunk.ticking.response.TickContinue;
import dev.twunk.ticking.response.TickResponse;
import dev.twunk.ticking.response.TickSleep;
import dev.twunk.ticking.response.TickStop;
import dev.twunk.ticking.strategy.TickStrategy;
import dev.twunk.utils.Benchmark;
import java.util.ArrayList;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PLAN:
 * - im okay with onAdd being slow, i don't like it but hey
 */

/**
 * NOTE -> SmartTickSystem is STRONGLY LINKED and COMPLETELY OWNS the SmartTickInfo component
 * and i WILL mercilessly throw it onto all entities I manage
 */

/**
 * Plan is: this system is NOT a normal query system. This is a "smart" system
 * that has another
 * tick method.
 *
 * It contains its queried objects, or rather its filter yeah its query has a
 * new option, field.
 *
 * I need a special system for components that work with Query.field() -> they
 * must call
 * an update on themselves when a given field that's referenced is updated. More
 * so, when a set operation happens, they must register somewhere that they did
 * the set. specifically, my system wants to know when a given field changes,
 *
 * so basically it's an observed component. i think?
 *
 * cause yeah i want to reduce how often this component gets queried and goes in
 * and out of this system
 *
 * and i want the component to not stress for implementors that don't care about
 * this
 *
 * nah
 *
 * i could really just do a thing where the system gets to have a isTicking
 * field
 * nah
 *
 * ok
 * so
 *
 * you do a query, and it gives you ANYTHING back. and i mean anything
 *
 * then you can return from the tick method in our system if we should tick it
 * next
 * tick too (or sleep it etc)
 *
 * then the system dynamically updates whether or not it gets called, and
 * doesn't even
 * check it until it needs to be called
 *
 * worry about saving that data later riley
 *
 * first: just get the components into here.
 *
 * i need
 * - discovery of components (can do a sub-system for this that does
 * onEntityAdd)
 *
 * now that i've got my discovery of components i need
 * - buckets for these components -> ticking, sleeping (with wakeup), sleeping
 * (no wakeup), stopped
 *
 * now that they're in the buckets, i tick my components
 * and if they return sleep or stop i move them to the relevant bucket.
 *
 * sleep should be a data structure heap thing that gives me the most soon
 * wakeup
 *
 * oh and i should probably sort my components but ignore that for now (for
 * efficient removal)
 * - consider a linked list -> good for going forward but not great otherwise.
 * - an array would be better for general running foward
 *
 * but fuck it, for now i'll just do an array and assume that this doesn't
 * change all that often. i'll push to the back so we have fifo anyway, meaning
 * if we've got cycling objects we're good
 *
 * meaning, since stuff is PROBABLY cyclic, i should do a circular array ->
 * something i should just steal from a library
 *
 * ok, so, my pre-system gives my system the components it needs and sends
 * events through about components getting added and remove
 * - sends my system new components
 * - trashes old components from my system
 *
 * my system then runs
 * - it stores stuff in buckets,
 * - it runs a pre-tick where it
 * --- trashes old components from the system as requested by the pre-system
 * --- then adds new components to the system (pushing to end of lists)
 * --- un-sleeps slept items
 * - then it runs the actual tick
 * --- stores results of the tick in a fixed size array list (same size as the
 * ..- ticking components and of type @Nullable TickResponse)
 * - then it runs through the tick responses one by one, managing the buckets as
 * a result.
 *
 * LOADING MY SYSTEM: need either each component to remember its state for MY
 * system (not a great idea if i don't have fixed IDs), or have my system
 * remember its state when loaded (also not a great idea if i don't have fixed
 * IDs)
 *
 * so basically, i need fixed IDs for my system.
 *
 * i could have a tickStates component that i own and register that lives on
 * each block that's managed by my system, where i store a map of "systemId" to
 * "TickState"
 *
 * yeah i like that idea a bit more, means that when i load the component in i
 * have the state already there, meaning i don't need to worry about fetching
 * it, OH and it means if it's NOT got that state, then i HAVEN'T ever seen it
 * before and it's just been added to the world FR
 *
 *
 *
 * // OKAY
 *
 * so effectively 3 systems but the last two are built into the same one
 * pre-system for events
 * system-pre-tick
 * system-tick
 *
 */
public abstract class SmartTickSystem {

    // each system gets a generated int ID so I can access info from it faster
    private static int nextId = 0;

    @Nonnull
    @SuppressWarnings("null")
    private static ComponentType<ChunkStore, BlockModule.BlockStateInfo> BLOCK_INFO_COMPONENT_TYPE =
        BlockModule.BlockStateInfo.getComponentType();

    @Nonnull
    @SuppressWarnings("null")
    private static ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT_TYPE = WorldChunk.getComponentType();

    ////////////////////////////////////////
    ////////////////////////////////////////
    // We store refs to all entities that match the query
    //
    // Notably, we segment these all out into ticking, sleeping, etc
    // with the idea being -> we'll probably want to tick tons of stuff
    // and so the locality (and ease) of dumping it all into a list makes our
    // life really easy
    ////////////////////////////////////////

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

    ////////////////////////////////////////
    ////////////////////////////////////////
    // Each SmartTickSystem needs a unique ID (so i can do funky stuff and tracking
    // of things in the background efficiently)
    //
    // Don't worry, to run out of IDs you'd have to make, billions of systems.
    //
    // we'll be fine.
    //
    // Importantly, I wanted to hide the "pre" and "post" systems and internal
    // details leaving just what any of us care about -> "i want code to run
    // on my thing". Done. Treat "SmartTickSystem" as a single system.
    //
    // IF YOU NEED TO SETUP DEPENDENCIES AROUND THIS just keep in mind that
    // these systems execute in order
    // 1) EntityRegister
    // 2) EntityTickerx
    ////////////////////////////////////////

    /**
     * Needs to be globally unique. This is how you save/load data. Don't lose it,
     * and if you change it, please make sure to migrate from the old one so players
     * don't lose all their data
     */
    public final int id;

    @Nonnull
    public final EntityRegister registrationSystem;

    @Nonnull
    public final EntityTicker tickingSystem;

    public SmartTickSystem(@Nonnull String id) {
        this.id = nextId++;
        this.registrationSystem = new EntityRegister();
        this.tickingSystem = new EntityTicker();
    }

    /**
     * The query for the compoennts you'll be smart-ticking on
     */
    public abstract Query<ChunkStore> getQuery();

    /**
     * Define how often you want your system to tick
     */
    @Nonnull
    public TickStrategy getTickStrategy() {
        return TickStrategy.always();
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
    public TickResponse onTick(
        @Nonnull TickingEntityMetadata state,
        float dt,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // console.log("Tick! " + ref + "  " + ticking.size());

        return null;
    }

    /**
     * Setup your system with your plugin. Yup. That's about it.
     *
     * @param plugin
     */
    public final void registerToPlugin(ModPlugin plugin) {
        plugin.getChunkStoreRegistry().registerSystem(registrationSystem);
        plugin.getChunkStoreRegistry().registerSystem(tickingSystem);
    }

    ////////////////////////////////////////
    ////////////////////////////////////////
    // Unique sub-system for each entity register
    ////////////////////////////////////////

    /**
     * Listens for add and remove events for any entity that matches our query, then
     * communicates this to the entity ticker.
     *
     * Means that we don't have to run something for ALL entities that match our
     * query every tick, and can instead track ther life ourselves so we ONLY run
     * the entities that are ticking (with minimal mutations to components etc)
     */
    public class EntityRegister extends RefSystem<ChunkStore> {

        // Component type of the place (on each entity matching our query) that i store
        // whether it is ticking, sleeping etc
        @Nonnull
        private static final ComponentType<ChunkStore, SmartTickingInfo> TICK_STATE_COMPONENT =
            SmartTickingInfo.getComponentType();

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
            return SmartTickSystem.this.getQuery();
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
            var systemState = tickingInfo.getTickingInfo(SmartTickSystem.this);
            if (systemState == null) {
                systemState = new TickContinue();
                tickingInfo.setTickingInfo(SmartTickSystem.this, systemState);
            }

            return tickingInfo;
        }

        /**
         * Get a ref to the list in which this entity should be placed based on its
         * current ticking state (active, sleeping, stopped etc)
         * @return
         */
        @Nonnull
        private final ArrayList<TickingEntityMetadata> getEntityTickGroup(TickResponse currentState) {
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
            // figure out the current/initial ticking state our entity has
            var tickingInfo = loadEntityTickingState(ref, commandBuffer);
            var initialState = tickingInfo.getTickingInfo(SmartTickSystem.this);

            // Figure out which tick group we should put our entity in based on its
            // state
            var area = getEntityTickGroup(initialState);

            // prepare the variables/references we need to run our tick method
            // (whenever that tick happens)
            var onTickCache = getTickVars(ref, store, area);
            if (onTickCache == null) {
                return;
            }

            // we'll put chuck our cache into the right ticking group (ready to go)
            area.add(onTickCache);

            // and finally, we'll write down the area that we put said cache
            // so our component (found by ref) can remove itself easily
            // when it gets removed
            tickingInfo._setMemoryLocation(SmartTickSystem.this, onTickCache);
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
            this.dropEntity(ref, reason, store);
        }

        /**
         * Run cleanup for the entity being unloaded/removed
         */
        private void dropEntity(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final RemoveReason reason,
            @Nonnull final Store<ChunkStore> store
        ) {
            store.getComponent(ref, TICK_STATE_COMPONENT).drop(SmartTickSystem.this, reason);
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
    public class EntityTicker extends ArchetypeTickingSystem<ChunkStore> {

        public EntityTicker() {}

        @SuppressWarnings({ "null", "rawtypes", "unchecked" })
        @Nonnull
        private final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(
            new SystemDependency(Order.AFTER, registrationSystem.getClass())
        );

        public void tick(float dt, int systemIndex, @Nonnull Store<ChunkStore> store) {
            store.tick(this, dt, systemIndex);
        }

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
            this._preTick(dt, archetypeChunk, store, commandBuffer);
            this._tick(dt, archetypeChunk, store, commandBuffer);
        }

        /**
         * Process incoming events (entites added or removed, figure out what sleeping
         * stuff needs to wake, etc) before ticking
         *
         * basically, get us into a valid state before doing a tick, or as valid as we
         * can be at least in the current instant
         */
        private void _preTick(
            float dt,
            @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer
        ) {
            // ++currTick;
            // if (currTick % 300 == 0) {
            //     console.log(String.format("STATS:\n - ticking: %d", ticking.size()));
            //     var i = 0;
            //     for (var entity : ticking) {
            //         console.log(
            //             String.format("   %2s) (%d, %d, %d)", ("" + i), entity.pos.x, entity.pos.y, entity.pos.z)
            //         );
            //     }
            // }
        }

        /**
         * Run ticks on all awake entities that match the parent's query
         */
        private void _tick(
            float dt,
            @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer
        ) {
            Benchmark.timeFuncExecution("Thing", () -> {
                // tick all our refs (ugly loop syntaxt basically just to assert that it's not
                // null. trust me, it's not, i guarantee it on its way in elsewhere, just want
                // to avoid having to check something the java type system can't pick up for
                // some reason)
                for (@Nonnull
                @SuppressWarnings("null")
                var ticker : ticking) {
                    // need to make it so that we check if the ref is still valid at this stage
                    // (eventually)
                    var res = SmartTickSystem.this.onTick(ticker, dt, store, commandBuffer);

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
            });
        }

        @Override
        @Nullable
        public Query<ChunkStore> getQuery() {
            return SmartTickSystem.this.getQuery();
        }

        /**
         * must run AFTER the its own entity register system
         */
        @Nonnull
        @Override
        public Set<Dependency<ChunkStore>> getDependencies() {
            return DEPENDENCIES;
        }
    }
}
