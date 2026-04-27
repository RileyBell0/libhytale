package dev.twunk.lib.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;

public abstract class OnTick__Listener<ECS_TYPE extends WorldProvider> extends OnTick<ECS_TYPE> {

    private final IOnTick<ECS_TYPE> listener;

    protected OnTick__Listener(IRegistry<ECS_TYPE> registry, Query<ECS_TYPE> query, IOnTick<ECS_TYPE> listener) {
        super(registry, query);
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

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
