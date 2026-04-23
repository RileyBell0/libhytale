package dev.twunk.lib.events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.events.OnTick;
import dev.twunk.hytale.interfaces.events.IOnTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.refs.AnyRef;

public abstract class OnTick__Listener<ECS_TYPE extends WorldProvider> extends OnTick<ECS_TYPE> {

    private final IOnTick<ECS_TYPE> listener;

    public OnTick__Listener(IOnTick<ECS_TYPE> listener, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        super(query, registry);
        this.listener = listener;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public final void tick(
        float dt,
        int index,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onTick(dt, new AnyRef<>(archetypeChunk.getReferenceTo(index)), commandBuffer);
    }
}
