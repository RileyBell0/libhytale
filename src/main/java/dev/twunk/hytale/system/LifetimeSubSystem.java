package dev.twunk.hytale.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.ILifetime;
import dev.twunk.interfaces.methods.ITick;

/**
 * Tiny Subsystem to simply tell our parent system when we added/removed entities
 * that match our parent's query
 *
 * GOAL: Need to know when entities load/unload (and optionally why they got added/removed)
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - ILifetimeSystem runner
 *
 * My code
 * @see ILifetime       - Methods for listening to entity add/remove events
 * @see TickSubSystem   - Underlying SubSystem that powers the IEntityTick methods
 *                              for IEntityTickSystems that register an EntityTickSubSystem
 * @see ITick           - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per matching entity, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public abstract class LifetimeSubSystem<ECS_STORE extends WorldProvider>
    extends RefSystem<ECS_STORE>
    implements ISubSystem<ECS_STORE>
{

    private final ILifetime<ECS_STORE> listener;
    private final Query<ECS_STORE> query;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    @SuppressWarnings("unchecked")
    public static <ECS_STORE extends WorldProvider, T extends LifetimeSubSystem<ECS_STORE>> LifetimeSubSystem<
        ECS_STORE
    > newSubsystemFor(final ILifetime<ECS_STORE> listener, final Query<ECS_STORE> query) {
        return ISubSystem.__newSubSystem(LifetimeSubSystem.class, ILifetime.class, listener, query);
    }

    protected LifetimeSubSystem(final ILifetime<ECS_STORE> listener, final Query<ECS_STORE> query) {
        this.listener = listener;
        this.query = query;
    }

    @Override
    public void onEntityAdded(
        Ref<ECS_STORE> ref,
        AddReason reason,
        Store<ECS_STORE> store,
        CommandBuffer<ECS_STORE> commandBuffer
    ) {
        listener.onEntityAdded(new AnyRef<>(ref), reason, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        Ref<ECS_STORE> ref,
        RemoveReason reason,
        Store<ECS_STORE> store,
        CommandBuffer<ECS_STORE> commandBuffer
    ) {
        listener.onEntityRemove(new AnyRef<>(ref), reason, commandBuffer);
    }

    @Override
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }
}
