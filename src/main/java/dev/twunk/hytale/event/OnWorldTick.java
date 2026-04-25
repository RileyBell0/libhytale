package dev.twunk.hytale.event;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.ISystemEventDriver;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.event.OnWorldTick__Listener;
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
public abstract class OnWorldTick<ECS_TYPE extends WorldProvider>
    extends ArchetypeTickingSystem<ECS_TYPE> // hytale's underlying driver for my code
    implements ISystemEventDriver<ECS_TYPE>
{

    private final @Nullable Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    protected OnWorldTick(Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.query = query;
        this.registry = registry;
    }

    @Override
    @Nullable
    public final Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region hide

    /**
     * Shim around other method for reducing boilerplate if i define a query on my class
     */
    public static final <
        ECS_TYPE extends WorldProvider,
        T extends IOnWorldTick<ECS_TYPE> & IQuery<ECS_TYPE>
    > OnWorldTick<ECS_TYPE> newDriverFor(T listener, IRegistry<ECS_TYPE> registry) {
        return newDriverFor(listener, listener.getQuery(), registry);
    }

    public static final <ECS_TYPE extends WorldProvider> OnWorldTick<ECS_TYPE> newDriverFor(
        IOnWorldTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnWorldTick__Listener.class,
                IOnWorldTick.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    // #endregion hide
}
