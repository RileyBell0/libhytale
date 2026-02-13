package dev.twunk.system;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.system.interfaces.ISubSystem;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SubSystem implements QuerySystem<ChunkStore> {

    @Nonnull
    private final ArrayList<ISubSystem> subSystems = new ArrayList<>();

    @Nonnull
    private final Query<ChunkStore> query;

    public SubSystem(@Nonnull Query<ChunkStore> query) {
        this.query = query;
    }

    public void appendSubSystem(ISubSystem system) {
        this.subSystems.add(system);
    }

    public void registerTo(ModPlugin plugin) {
        for (var system : subSystems) {
            system.registerTo(plugin);
        }
    }

    @Override
    @Nullable
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
