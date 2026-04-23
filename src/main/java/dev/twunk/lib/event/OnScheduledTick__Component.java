package dev.twunk.lib.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.lib.event.scheduled.TickPlan;
import dev.twunk.lib.event.scheduled.TrackedEntity;
import javax.annotation.Nullable;

public class OnScheduledTick__Component<
    ECS_TYPE extends WorldProvider,
    T extends Component<ECS_TYPE>
> extends OnScheduledTick<ECS_TYPE> {

    private final ComponentType<ECS_TYPE, T> componentType;

    public OnScheduledTick__Component(
        String id,
        ComponentType<ECS_TYPE, T> componentType,
        IRegistry<ECS_TYPE> registry
    ) {
        super(id, Query.and(componentType), registry);
        this.componentType = componentType;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public final TickPlan runScheduledTick(
        TrackedEntity<ECS_TYPE> ticker,
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        final var component = ComponentUtils.get(ticker, componentType);
        if (component == null) {
            return null;
        }

        return ((IOnScheduledTick<ECS_TYPE>) component).onScheduledTick(dt, ticker, commandBuffer);
    }
}
