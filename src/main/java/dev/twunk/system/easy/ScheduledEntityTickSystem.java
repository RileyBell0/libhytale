package dev.twunk.system.easy;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.system.response.TickResponse;
import dev.twunk.system.smart.TickingEntityMetadata;
import dev.twunk.utils.TrackedEntities;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Composite subsystem to allow the parent to run code on its elements every
 * tick in a smarter way
 *
 * GOAL: need to tick my entities that match the query but not necessarily EVERY
 *       tick. Need logic to determine if my entity should sleep/continue ticking
 *       without changing its components constantly
 */
public class ScheduledEntityTickSystem implements ISubSystem, IEntityLifetimeSystem, ISystemTick, IScheduledEntityTick {

    private static int nextId = 0;

    // at some point i need a better method -> you'll have to provide an ID and
    // i'll mangle it into a format for me that guarnatees uniqueness
    // cause, i need a way to know WHICH system owned the stored data from a block
    // when we reload stuff in potentially different orders
    public final int id;

    @Nonnull
    private final ArrayList<ISubSystem> subSystems = new ArrayList<>();

    @Nonnull
    private final TrackedEntities entities;

    @Nonnull
    private final LifetimeSystem lifetimeSystem = new LifetimeSystem(this);

    @Nonnull
    private final GlobalTickSystem entityTickSystem = new GlobalTickSystem(this);

    @Nonnull
    private final Query<ChunkStore> query;

    public ScheduledEntityTickSystem(@Nonnull Query<ChunkStore> query) {
        this.id = nextId++;
        this.entities = new TrackedEntities(this.id);
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
    public TickResponse onEntityTick(
        @Nonnull TickingEntityMetadata state,
        float dt,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        return null;
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

    /**
     * When an entity is added to the world that matches our query, we figure out
     * if it IS ticking already (or sleeping etc), and if it DOESN'T have a state
     * for our given system, we add one, a default.
     */
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
     * Handles pre-tick tasks
     * - waking up sleeping entities
     * And then handles ticking tasks
     * - ticks all awake entities that match the query
     * - handles the return from the ticking entities to put them to sleep etc (if
     * need be. really just to change their state if requested)
     *
     * NOTE: This one does NOT have a tick method for you to call, it makes no sense on its own without
     * considering the parent ScheduledEntityTickSystem
     *
     * tick method that gets called by the `store`
     * this is pretty much just a shim to get into my code, as i don't want to touch
     * theirs wherever possible
     */
    public void onSystemTick(
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
            var res = ScheduledEntityTickSystem.this.onEntityTick(ticker, dt, store, commandBuffer);

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

    public void addSubSystem(@Nonnull ISubSystem system) {
        this.subSystems.add(system);
    }

    // Custom register for our ScheduledEntityTickSystem
    //
    // why?
    // because it's not one system, it's two working in conjunction
    public void registerTo(ModPlugin plugin) {
        lifetimeSystem.registerTo(plugin);
        entityTickSystem.registerTo(plugin);
    }

    @Override
    @Nullable
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
