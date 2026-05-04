package dev.twunk.hytale.event.composite;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IQueryableEventDriver;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public class QueryableCompositeSystem<ECS_TYPE extends WorldProvider, Listener>
    extends CompositeSystem<ECS_TYPE, Listener>
    implements IQueryableEventDriver<ECS_TYPE>
{

    protected final Query<ECS_TYPE> query;

    protected QueryableCompositeSystem(IRegistry<ECS_TYPE> registry, Query<ECS_TYPE> query, Listener listener) {
        super(registry, listener);
        this.query = query;
    }

    @Override
    public final Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final Query<ECS_TYPE> getQuery(Class<?> clazz) {
        return this.query;
    }
}
