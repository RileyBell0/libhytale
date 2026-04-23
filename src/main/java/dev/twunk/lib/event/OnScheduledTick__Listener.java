package dev.twunk.lib.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.event.scheduled.TickPlan;
import dev.twunk.lib.event.scheduled.TrackedEntity;
import javax.annotation.Nullable;

public class OnScheduledTick__Listener<ECS_TYPE extends WorldProvider> extends OnScheduledTick<ECS_TYPE> {

    private final IOnScheduledTick<ECS_TYPE> listener;

    public OnScheduledTick__Listener(
        String id,
        IOnScheduledTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        super(id, query, registry);
        this.listener = listener;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    @Nullable
    public final TickPlan tickTheTicker(
        TrackedEntity<ECS_TYPE> ticker,
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        return listener.onScheduledTick(dt, ticker, commandBuffer);
    }
}
