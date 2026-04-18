package dev.twunk.hytale.system.ignoreme;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.system.OnAddRemove;
import dev.twunk.interfaces.events.IOnAddRemove;
import dev.twunk.interfaces.methods.IRegistry;

/**
 * This version will call the add and remove events defined in a specific component instance
 *
 * it's the exact same as a system that fetches your component then calls the method on it
 */
public abstract class OnAddRemove__Component<
    ECS_TYPE extends WorldProvider,
    T extends Component<ECS_TYPE>
> extends OnAddRemove<ECS_TYPE> {

    private final ComponentType<ECS_TYPE, T> componentType;

    protected OnAddRemove__Component(ComponentType<ECS_TYPE, T> componentType, IRegistry<ECS_TYPE> registry) {
        super(Query.and(componentType), registry);
        this.componentType = componentType;
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
        var anyRef = new AnyRef<>(ref);

        // java is weird and won't let me define T to be both IOnAddRemove and Component, so i have the `init` define
        // this bound for me (because i can if its on a static method???) and then i'll just cast it here which IS SAFE given i've got that
        // bound guarnateed earlier
        @SuppressWarnings("unchecked")
        var component = (IOnAddRemove<ECS_TYPE>) anyRef.getComponent(componentType);
        if (component == null) {
            return;
        }

        component.onEntityAdded(anyRef, reason, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        Ref<ECS_TYPE> ref,
        RemoveReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        var anyRef = new AnyRef<>(ref);

        // java is weird and won't let me define T to be both IOnAddRemove and Component, so i have the `init` define
        // this bound for me (because i can if its on a static method???) and then i'll just cast it here which IS SAFE given i've got that
        // bound guarnateed earlier
        @SuppressWarnings("unchecked")
        var component = (IOnAddRemove<ECS_TYPE>) anyRef.getComponent(componentType);
        if (component == null) {
            return;
        }

        component.onEntityRemove(anyRef, reason, commandBuffer);
    }
}
