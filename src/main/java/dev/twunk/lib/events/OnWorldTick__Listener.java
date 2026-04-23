package dev.twunk.lib.events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.events.OnWorldTick;
import dev.twunk.interfaces.events.IOnWorldTick;
import dev.twunk.interfaces.methods.IRegistry;

public abstract class OnWorldTick__Listener<ECS_TYPE extends WorldProvider> extends OnWorldTick<ECS_TYPE> {

    private final IOnWorldTick<ECS_TYPE> listener;

    public OnWorldTick__Listener(IOnWorldTick<ECS_TYPE> listener, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        super(query, registry);
        this.listener = listener;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public final void tick(
        final float dt,
        final ArchetypeChunk<ECS_TYPE> archetypeChunk,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        this.listener.onWorldTick(dt, archetypeChunk, store, commandBuffer);
    }
}
