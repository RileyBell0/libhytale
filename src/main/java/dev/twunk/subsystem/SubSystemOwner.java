package dev.twunk.subsystem;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.IRegistryProvider;
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
public abstract class SubSystemOwner<
    ECS_STORE extends WorldProvider
> implements IQuery<ECS_STORE>, IRegistryProvider<ECS_STORE> {

    @Nonnull
    private final ArrayList<ISubSystem<ECS_STORE>> subSystems = new ArrayList<>();

    @Nonnull
    private final Query<ECS_STORE> query;

    public SubSystemOwner(final @Nonnull Query<ECS_STORE> query) {
        this.query = query;
    }

    protected void appendSubSystem(final @Nonnull ISubSystem<ECS_STORE> system) {
        this.subSystems.add(system);
    }

    public void registerTo(final @Nonnull ModPlugin plugin) {
        for (final var system : subSystems) {
            system.registerTo(plugin);
        }
    }

    @Override
    @Nonnull
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }
}
