package dev.twunk.lib.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnWorldTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public class OnWorldTick__Listener<ECS_TYPE extends WorldProvider> extends OnWorldTick<ECS_TYPE> {

    private final IOnWorldTick<ECS_TYPE> listener;

    protected OnWorldTick__Listener(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnWorldTick<ECS_TYPE> listener
    ) {
        super(registry, query);
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public void tick(
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onWorldTick(dt, archetypeChunk, store, commandBuffer);
    }
}
