package dev.twunk.hytale.interfaces.config;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;

public interface IQuery<ECS_TYPE extends WorldProvider> {
    public Query<ECS_TYPE> getQuery();

    public default Query<ECS_TYPE> getQuery(Class<?> clazz) {
        return this.getQuery();
    }
}
