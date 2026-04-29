package dev.twunk.lib.event;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.event.OnAddRemove__Listener;

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
public class OnAddRemove__Listener<ECS_TYPE extends WorldProvider> extends OnAddRemove<ECS_TYPE> {

    /**
     * YOUR class (well, instance of it). I store a reference to it here so i can keep calling
     * the event listener methods on it when event happen
     */
    private final IOnAddRemove<ECS_TYPE> listener;

    protected OnAddRemove__Listener(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnAddRemove<ECS_TYPE> listener
    ) {
        super(registry, query);
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public void onEntityAdded(
        Ref<ECS_TYPE> ref,
        AddReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onEntityAdded(ref, reason, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        Ref<ECS_TYPE> ref,
        RemoveReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onEntityRemove(ref, reason, commandBuffer);
    }
}
