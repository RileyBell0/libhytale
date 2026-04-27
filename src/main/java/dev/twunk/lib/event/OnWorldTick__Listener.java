package dev.twunk.lib.event;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnWorldTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public class OnWorldTick__Listener<ECS_TYPE extends WorldProvider> extends OnWorldTick<ECS_TYPE> {

    private final IOnWorldTick<ECS_TYPE> listener;

    protected OnWorldTick__Listener(IRegistry<ECS_TYPE> registry, IOnWorldTick<ECS_TYPE> listener) {
        super(registry);
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public final void tick(float dt, int index, Store<ECS_TYPE> store) {
        this.listener.onWorldTick(dt, index, store);
    }
}
