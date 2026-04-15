package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IRegistry;
import dev.twunk.interfaces.methods.IUniverseTick;
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
public class UniverseTickSubSystem<ECS_STORE extends WorldProvider>
    extends ArchetypeTickingSystem<ECS_STORE>
    implements ISubSystem<ECS_STORE>
{

    private final IUniverseTick<ECS_STORE> listener;
    private final @Nullable Query<ECS_STORE> query;
    private final IRegistry<ECS_STORE> registry;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_STORE extends WorldProvider> UniverseTickSubSystem<ECS_STORE> constructNewSystemClass(
        IUniverseTick<ECS_STORE> listener,
        Query<ECS_STORE> query,
        IRegistry<ECS_STORE> registry
    ) {
        return ISubSystem.__construct(
            ISubSystem.__dupeClassAndGetConstructor(
                UniverseTickSubSystem.class,
                IUniverseTick.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    protected UniverseTickSubSystem(
        IUniverseTick<ECS_STORE> listener,
        Query<ECS_STORE> query,
        IRegistry<ECS_STORE> registry
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
        final ArchetypeChunk<ECS_STORE> archetypeChunk,
        final Store<ECS_STORE> store,
        final CommandBuffer<ECS_STORE> commandBuffer
    ) {
        listener.onSystemTick(dt, archetypeChunk, store, commandBuffer);
    }

    @Override
    @Nullable
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_STORE> getRegistry() {
        return this.registry;
    }
}
