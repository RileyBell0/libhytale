package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.events.IOnWorldTick;
import dev.twunk.interfaces.methods.IQuery;
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
 * @see IOnWorldTick - Something that this subsystem can call and run.
 *
 * Hytale's code
 * @see ArchetypeTickingSystem - I use this to run the subsystem. Only way i currently know
 *                               of for getting a commandBuffer in a global tick
 */
public abstract class OnWorldTickEventDriver<ECS_TYPE extends WorldProvider>
    extends ArchetypeTickingSystem<ECS_TYPE> // hytale's underlying driver for my code
    implements ISubSystem<ECS_TYPE>
{

    private final @Nullable Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnWorldTickEventDriver<ECS_TYPE> newUninitialised(
        IOnWorldTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return ISubSystem.__construct(
            ISubSystem.__dupeClassAndGetConstructor(
                OnWorldTickEventDriver.class,
                IOnWorldTick.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    protected OnWorldTickEventDriver(Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.query = query;
        this.registry = registry;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * tick method that gets called by the `store`
     * this is pretty much just a shim to get into my code, as i don't want to touch
     * theirs wherever possible
     */
    @Override
    public abstract void tick(
        final float dt,
        final ArchetypeChunk<ECS_TYPE> archetypeChunk,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
    );

    @Override
    @Nullable
    public final Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    public final class ForListener extends OnWorldTickEventDriver<ECS_TYPE> {

        private final IOnWorldTick<ECS_TYPE> listener;

        protected ForListener(IOnWorldTick<ECS_TYPE> listener, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
            super(query, registry);
            this.listener = listener;
        }

        /**
         * Shim around other method for reducing boilerplate if i define a query on my class
         */
        public static final <
            ECS_TYPE extends WorldProvider,
            T extends IOnWorldTick<ECS_TYPE> & IQuery<ECS_TYPE>
        > OnWorldTickEventDriver<ECS_TYPE> newUninitialised(T listener, IRegistry<ECS_TYPE> registry) {
            return newUninitialised(listener, listener.getQuery(), registry);
        }

        public static final <ECS_TYPE extends WorldProvider> OnWorldTickEventDriver<ECS_TYPE> newUninitialised(
            IOnWorldTick<ECS_TYPE> listener,
            Query<ECS_TYPE> query,
            IRegistry<ECS_TYPE> registry
        ) {
            return ISubSystem.__construct(
                ISubSystem.__dupeClassAndGetConstructor(
                    OnWorldTickEventDriver.ForListener.class,
                    IOnWorldTick.class,
                    Query.class,
                    IRegistry.class
                ),
                listener,
                query,
                registry
            );
        }

        ///////////////////////////////////////////////////////////////////////////
        // \/======================\/-  Methods  -\/==========================\/ //
        ///////////////////////////////////////////////////////////////////////////

        @Override
        public final void tick(
            final float dt,
            final ArchetypeChunk<ECS_TYPE> archetypeChunk,
            final Store<ECS_TYPE> store,
            final CommandBuffer<ECS_TYPE> commandBuffer
        ) {
            this.listener.onWorldTick(dt, archetypeChunk, store, commandBuffer);
        }
    }
}
