package dev.twunk.lib.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.event.OnBlockTick;
import dev.twunk.hytale.interfaces.event.IOnBlockTick;
import dev.twunk.hytale.ref.BlockRef;

// <ECS_TYPE extends WorldProvider>
public abstract class OnBlockTick__Component<T extends Component<ChunkStore>> extends OnBlockTick {

    private final ComponentType<ChunkStore, T> componentType;

    protected OnBlockTick__Component(Query<ChunkStore> query, ComponentType<ChunkStore, T> componentType) {
        super(query);
        this.componentType = componentType;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public final void tick(
        float dt,
        int index,
        ArchetypeChunk<ChunkStore> archetypeChunk,
        Store<ChunkStore> store,
        CommandBuffer<ChunkStore> commandBuffer
    ) {
        var ref = new BlockRef(archetypeChunk.getReferenceTo(index));

        var component = (IOnBlockTick) ref.getComponent(componentType);
        if (component == null) {
            return;
        }

        component.onBlockTick(ref, commandBuffer);
    }
}
