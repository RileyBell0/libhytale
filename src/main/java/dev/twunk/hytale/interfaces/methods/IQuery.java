package dev.twunk.hytale.interfaces.methods;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;

/**
 * Simply a NON NULL method that returns a Query (used in Systems)
 */
public interface IQuery<ECS_TYPE extends WorldProvider> {
    public Query<ECS_TYPE> getQuery();
}
