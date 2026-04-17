package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IOnUniverseTick;
import dev.twunk.interfaces.methods.IRegistry;
import javax.annotation.Nullable;

/**
 * Subsystem for calling `onSystemTick` on the parent system every tick
 *
 * GOAL: run code ONCE per tick globally. not per element, just, run this once per tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IGlobalTickSystem runner
 *
 * My code
 * @see IUniverseTickSystem - Something that this subsystem can call and run.
 *
 * Hytale's code
 * @see ArchetypeTickingSystem - I use this to run the subsystem. Only way i currently know
 *                               of for getting a commandBuffer in a global tick
 */
public class OnUniverseTickSystem<ECS_TYPE extends WorldProvider>
    extends ArchetypeTickingSystem<ECS_TYPE>
    implements ISubSystem<ECS_TYPE>
{

    private final IOnUniverseTick<ECS_TYPE> listener;
    private final @Nullable Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnUniverseTickSystem<ECS_TYPE> constructNewSystemClass(
        IOnUniverseTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return ISubSystem.__construct(
            ISubSystem.__dupeClassAndGetConstructor(
                OnUniverseTickSystem.class,
                IOnUniverseTick.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    protected OnUniverseTickSystem(
        IOnUniverseTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        this.listener = listener;
        this.query = query;
        this.registry = registry;
    }

    /**
     * tick method that gets called by the `store`
     * this is pretty much just a shim to get into my code, as i don't want to touch
     * theirs wherever possible
     */
    @Override
    public void tick(
        final float dt,
        final ArchetypeChunk<ECS_TYPE> archetypeChunk,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onSystemTick(dt, archetypeChunk, store, commandBuffer);
    }

    @Override
    @Nullable
    public Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }
}
