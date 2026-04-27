package dev.twunk.lib.event;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnUniverseTick;
import dev.twunk.hytale.interfaces.event.IOnUniverseTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public class OnUniverseTick__Listener<ECS_TYPE extends WorldProvider> extends OnUniverseTick<ECS_TYPE> {

    private final IOnUniverseTick<ECS_TYPE> listener;

    protected OnUniverseTick__Listener(IRegistry<ECS_TYPE> registry, IOnUniverseTick<ECS_TYPE> listener) {
        super(registry);
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public final void tick(float dt, int index, Store<ECS_TYPE> store) {
        this.listener.onUniverseTick(dt, index, store);
    }
}
