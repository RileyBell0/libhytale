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
import dev.twunk.interfaces.methods.IOnAddRemove;
import dev.twunk.interfaces.methods.IOnTick;
import dev.twunk.interfaces.methods.IRegistry;

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
 * @see IOnAddRemove       - Methods for listening to entity add/remove events
 * @see OnTickSystem   - Underlying SubSystem that powers the IEntityTick methods
 *                              for IEntityTickSystems that register an EntityTickSubSystem
 * @see IOnTick           - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per matching entity, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public class OnAddRemoveSystem<ECS_TYPE extends WorldProvider>
    extends RefSystem<ECS_TYPE>
    implements ISubSystem<ECS_TYPE>
{

    private final IOnAddRemove<ECS_TYPE> listener;
    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider, T extends OnAddRemoveSystem<ECS_TYPE>> OnAddRemoveSystem<
        ECS_TYPE
    > constructNewSystemClass(IOnAddRemove<ECS_TYPE> listener, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        return ISubSystem.__construct(
            ISubSystem.__dupeClassAndGetConstructor(
                OnAddRemoveSystem.class,
                IOnAddRemove.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    protected OnAddRemoveSystem(IOnAddRemove<ECS_TYPE> listener, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.listener = listener;
        this.query = query;
        this.registry = registry;
    }

    @Override
    public void onEntityAdded(
        Ref<ECS_TYPE> ref,
        AddReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onEntityAdded(new AnyRef<>(ref), reason, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        Ref<ECS_TYPE> ref,
        RemoveReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onEntityRemove(new AnyRef<>(ref), reason, commandBuffer);
    }

    @Override
    public Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }
}
