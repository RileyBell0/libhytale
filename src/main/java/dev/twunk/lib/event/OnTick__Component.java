package dev.twunk.lib.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;

public abstract class OnTick__Component<
    ECS_TYPE extends WorldProvider,
    T extends Component<ECS_TYPE>
> extends OnTick<ECS_TYPE> {

    private final ComponentType<ECS_TYPE, T> componentType;

    protected OnTick__Component(ComponentType<ECS_TYPE, T> componentType, IRegistry<ECS_TYPE> registry) {
        super(Query.and(componentType), registry);
        this.componentType = componentType;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public final void tick(
        float dt,
        int index,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        var ref = new AnyRef<>(archetypeChunk.getReferenceTo(index));

        @SuppressWarnings("unchecked")
        var component = (IOnTick<ECS_TYPE>) ref.getComponent(componentType);
        if (component == null) {
            return;
        }

        component.onTick(dt, ref, commandBuffer);
    }
}
