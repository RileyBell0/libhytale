package dev.twunk.hytale.system;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.annotations.EventRunners;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IQuery;
import java.util.ArrayList;

/**
 * Nice-to-have wrapper for making a system OR composite subsystem
 *
 * You don't need to use this to utilise the subsystems, but, you probably
 * should, or at least read it to see how i do stuff (and to understand WHY
 * if you choose to make your own)
 *
 * Makes it easy to register sub systems to it, and then easy to register the
 * overall parent itself (assuming the parent is just a regular class)
 *
 * Forces the parent to provide a `query` that its subsystems will use. That's
 * the most handy part honestly
 */
public abstract class SubSystemOwner<ECS_STORE extends WorldProvider> implements IQuery<ECS_STORE> {

    private final ArrayList<ISubSystem<ECS_STORE>> subSystems = new ArrayList<>();
    private final Query<ECS_STORE> query;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public SubSystemOwner(final Query<ECS_STORE> query) {
        this.query = query;

        this.init();
    }

    protected void init() {
        // look for event annotations on self
        EventRunners.Chunk chunkEvents;
        if ((chunkEvents = this.getClass().getAnnotation(EventRunners.Chunk.class)) != null) {
            for (var clazz : chunkEvents.value()) {}
        }

        EventRunners.Entity entityEvents;
        if ((entityEvents = this.getClass().getAnnotation(EventRunners.Entity.class)) != null) {
            for (var clazz : entityEvents.value()) {}
        }
    }

    protected void appendSubSystem(final ISubSystem<ECS_STORE> system) {
        this.subSystems.add(system);
    }

    public void registerTo(final HytalePlugin plugin) {
        for (final var system : subSystems) {
            system.registerTo(plugin);
        }
    }

    @Override
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }
}
