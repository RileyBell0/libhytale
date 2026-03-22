package dev.twunk.interfaces.methods;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import javax.annotation.Nonnull;

public interface IQuery<ECS_STORE extends WorldProvider> {
    @Nonnull
    public Query<ECS_STORE> getQuery();
}
