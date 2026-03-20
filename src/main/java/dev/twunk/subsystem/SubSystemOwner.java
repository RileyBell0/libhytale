package dev.twunk.subsystem;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.plugin.ModPlugin;
import java.util.ArrayList;
import javax.annotation.Nonnull;

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
public abstract class SubSystemOwner implements IQuery {

    @Nonnull
    private final ArrayList<ISubSystem> subSystems = new ArrayList<>();

    @Nonnull
    private final Query<ChunkStore> query;

    public SubSystemOwner(@Nonnull final Query<ChunkStore> query) {
        this.query = query;
    }

    protected void appendSubSystem(ISubSystem system) {
        this.subSystems.add(system);
    }

    public void registerTo(ModPlugin plugin) {
        for (var system : subSystems) {
            system.registerTo(plugin);
        }
    }

    @Override
    @Nonnull
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
