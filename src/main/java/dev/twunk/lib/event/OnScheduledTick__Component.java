package dev.twunk.lib.event;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.lib.event.scheduled.TickSchedule;
import javax.annotation.Nullable;

public class OnScheduledTick__Component<ECS_TYPE extends WorldProvider> extends OnScheduledTick<ECS_TYPE> {

    private final ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType;

    public <T extends Component<ECS_TYPE>> OnScheduledTick__Component(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        ComponentType<ECS_TYPE, T> componentType,
        String id
    ) {
        super(registry, query, id);
        this.componentType = componentType;
    }

    public <T extends Component<ECS_TYPE>> OnScheduledTick__Component(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        ComponentType<ECS_TYPE, T> componentType,
        String id,
        TickSchedule defaultSchedule
    ) {
        super(registry, query, id, defaultSchedule);
        this.componentType = componentType;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    @Nullable
    protected final TickSchedule _onScheduledTick(
        float dt,
        AnyRef<ECS_TYPE> ref,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        @SuppressWarnings("unchecked")
        final IOnScheduledTick<ECS_TYPE> component = (IOnScheduledTick<ECS_TYPE>) ComponentUtils.get(
            ref,
            this.componentType
        );
        if (component == null) {
            return null;
        }

        return component.onScheduledTick(dt, ref, commandBuffer);
    }
}
