package dev.twunk.lib.event;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;
import dev.twunk.lib.event.scheduled.TickSchedule;
import javax.annotation.Nullable;

public class OnScheduledTick__Listener<ECS_TYPE extends WorldProvider> extends OnScheduledTick<ECS_TYPE> {

    private final IOnScheduledTick<ECS_TYPE> listener;

    public OnScheduledTick__Listener(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnScheduledTick<ECS_TYPE> listener,
        String id
    ) {
        super(registry, query, id);
        this.listener = listener;
    }

    public OnScheduledTick__Listener(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnScheduledTick<ECS_TYPE> listener,
        String id,
        TickSchedule defaultSchedule
    ) {
        super(registry, query, id, defaultSchedule);
        this.listener = listener;
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
        return listener.onScheduledTick(dt, ref, commandBuffer);
    }
}
