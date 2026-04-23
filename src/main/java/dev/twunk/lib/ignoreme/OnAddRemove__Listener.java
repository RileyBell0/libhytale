package dev.twunk.lib.ignoreme;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.system.OnAddRemove;
import dev.twunk.interfaces.events.IOnAddRemove;
import dev.twunk.interfaces.methods.IRegistry;
import dev.twunk.lib.ignoreme.OnAddRemove__Listener;

/**
 * This is the normal version, an almost 1 to 1 of what hytale does. It's a "system"
 * that has its `onEntityAdded` and `onEntityRemove` methods called
 *
 * Only difference between this and base hytale is that YOUR code isn't actually
 * run in this system, you pass in a listener and i'll call your methods on that
 * listener
 *
 * its a bit if indirection that really helps to seperate out the boilerplate
 * of defining game functionality in your class from finagling the hytale codebase
 * to cooperate
 */
public abstract class OnAddRemove__Listener<ECS_TYPE extends WorldProvider> extends OnAddRemove<ECS_TYPE> {

    /**
     * YOUR class (well, instance of it). I store a reference to it here so i can keep calling
     * the event listener methods on it when event happen
     */
    private final IOnAddRemove<ECS_TYPE> listener;

    public OnAddRemove__Listener(IOnAddRemove<ECS_TYPE> listener, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        super(query, registry);
        this.listener = listener;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onEntityAdded(
        Ref<ECS_TYPE> ref,
        AddReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onEntityAdded(new AnyRef<>(ref), reason, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        Ref<ECS_TYPE> ref,
        RemoveReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onEntityRemove(new AnyRef<>(ref), reason, commandBuffer);
    }
}
