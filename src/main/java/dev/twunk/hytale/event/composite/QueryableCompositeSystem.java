package dev.twunk.hytale.event.composite;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IQueryableEventDriver;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public class QueryableCompositeSystem<ECS_TYPE extends WorldProvider>
    extends CompositeSystem<ECS_TYPE>
    implements IQueryableEventDriver<ECS_TYPE>
{

    protected final Query<ECS_TYPE> query;

    protected QueryableCompositeSystem(IRegistry<ECS_TYPE> registry, Query<ECS_TYPE> query) {
        super(registry);
        this.query = query;
    }

    @Override
    public Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public Query<ECS_TYPE> getQuery(Class<?> clazz) {
        return this.query;
    }
}
