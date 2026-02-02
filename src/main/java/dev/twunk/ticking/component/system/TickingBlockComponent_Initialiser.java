package dev.twunk.ticking.component.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.ITickingComponent;
import dev.twunk.ticking.response.TickContinue;
import dev.twunk.ticking.response.TickSleep;
import dev.twunk.ticking.response.TickStop;
import dev.twunk.utils.BlockUtils;
import javax.annotation.Nonnull;

public class TickingBlockComponent_Initialiser extends RefSystem<ChunkStore> {
    @SuppressWarnings("unused")
    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    private Query<ChunkStore> query;

    public TickingBlockComponent_Initialiser(Query<ChunkStore> query) {
        this.query = query;
    }

    // easy way to construct: if you pass in your class itself i'll just hook stuff
    // up with a default query
    public TickingBlockComponent_Initialiser(Class<? extends ITickingComponent> componentClass) {
        this.query = Query.and(
                BlockModule.BlockStateInfo.getComponentType(),
                ITickingComponent.getComponentType(componentClass));
    }

    /**
     * If you have blocks that you want to "tick", you need to "initialise" them
     * when they are loaded into the world or placed etc
     */
    @Override
    public void onEntityAdded(
            @Nonnull Ref<ChunkStore> ref,
            @Nonnull AddReason reason,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        ComponentType<ChunkStore, TickContinue> tickingAwake = TickContinue.COMPONENT_TYPE;

        // Ensure we have at least 1x ticking component (for queries)
        if (!BlockUtils.hasComponent(tickingAwake, commandBuffer, ref)
                && !BlockUtils.hasComponent(TickSleep.COMPONENT_TYPE, commandBuffer, ref)
                && !BlockUtils.hasComponent(TickStop.COMPONENT_TYPE, commandBuffer, ref)) {
            commandBuffer.ensureComponent(ref, tickingAwake);
        }
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<ChunkStore> ref,
            @Nonnull RemoveReason reason,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    }

    // Example: I override `getQuery` and use the following
    // `return Query.and(BlockModule.BlockResponseInfo.getComponentType(),
    // ExampleTickingComponent.getComponentType());`
    //
    // and since that seems to be a common pattern, i've made a constructor that you
    // can just pass your class
    // to, and given you've actually registered your component it'll "just work"
    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
