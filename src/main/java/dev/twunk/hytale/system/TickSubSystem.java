package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IRegistry;
import dev.twunk.interfaces.methods.ITick;

/**
 * Subsystem for calling `onEntityTick` on the parent system every tick
 *
 * GOAL: run code on entities every tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IEntityTickSystem runner
 *
 *
 * My code
 * @see ITick       - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per matching entity, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public class TickSubSystem<ECS_STORE extends WorldProvider>
    extends EntityTickingSystem<ECS_STORE>
    implements ISubSystem<ECS_STORE>
{

    private final ITick<ECS_STORE> listener;
    private final Query<ECS_STORE> query;
    private final IRegistry<ECS_STORE> registry;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static final <ECS_STORE extends WorldProvider, T extends TickSubSystem<ECS_STORE>> TickSubSystem<
        ECS_STORE
    > newSubsystemFor(ITick<ECS_STORE> listener, Query<ECS_STORE> query, IRegistry<ECS_STORE> registry) {
        return ISubSystem.__construct(
            ISubSystem.__dupeClassAndGetConstructor(TickSubSystem.class, ITick.class, Query.class, IRegistry.class),
            listener,
            query,
            registry
        );
    }

    protected TickSubSystem(ITick<ECS_STORE> listener, Query<ECS_STORE> query, IRegistry<ECS_STORE> registry) {
        this.listener = listener;
        this.query = query;
        this.registry = registry;
    }

    public void tick(
        float dt,
        int index,
        ArchetypeChunk<ECS_STORE> archetypeChunk,
        Store<ECS_STORE> store,
        CommandBuffer<ECS_STORE> commandBuffer
    ) {
        listener.onEntityTick(dt, new AnyRef<>(archetypeChunk.getReferenceTo(index)), commandBuffer);
    }

    @Override
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_STORE> getRegistry() {
        return this.registry;
    }
}
