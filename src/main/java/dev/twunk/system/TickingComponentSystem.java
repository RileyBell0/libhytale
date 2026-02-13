package dev.twunk.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.IRegisteredComponent;
import dev.twunk.component.ITickingComponent;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.utils.BlockUtils;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class TickingComponentSystem<T extends ITickingComponent> extends EntityTickingSystem<ChunkStore> {

    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    @Nonnull
    private final ComponentType<ChunkStore, T> tickingComponentType;

    @Nonnull
    private final Query<ChunkStore> query;

    public TickingComponentSystem(@Nonnull Supplier<ComponentType<ChunkStore, T>> supplier) {
        var val = supplier.get();
        if (val == null) {
            throw new RuntimeException("HECK supplier failed");
        }
        this.tickingComponentType = val;
        this.query = Query.and(this.tickingComponentType);
    }

    public TickingComponentSystem(@Nonnull Class<T> componentClass) {
        this.tickingComponentType = IRegisteredComponent.getComponentType(componentClass);
        this.query = Query.and(this.tickingComponentType);
    }

    public TickingComponentSystem(@Nonnull ComponentType<ChunkStore, T> tickingComponentType) {
        this.tickingComponentType = tickingComponentType;
        this.query = Query.and(this.tickingComponentType);
    }

    /**
     * No touchy. just read. overwrite it if you need
     */
    @Override
    public void tick(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // blockEntityRef --(has)-> blockInfo --(is in)-> worldChunk --(is in)-> world
        // blockInfo has local coords
        // blockInfo & worldChunk --(implies)-> global coords

        // ref to our block entity (ref to the entity at the current
        // index that matches the query - imagine the index as an arbtrary
        // `i` in a for loop. think nothing of it)
        var blockRef = archetypeChunk.getReferenceTo(index);

        // From my understanding, this seems to be a sort of inherent component
        // on block entites. You can always seem to get it. This stores its LOCAL
        // coordinates (sort of)
        //
        // It stores the `index` that the block is within its `chunk`. we do some
        // funky maths to turn that 1 dimensional index (1, 2, 3...) into a 3 dimensional
        // position [(x: 0, y: 0, z: 0), (x: 1, y: 0, z: 0)... etc]
        //
        // note: ^^ above numbers made up, really never checked which order they
        // index their blocks into the chunk
        var blockInfo = BlockUtils.getInfo(commandBuffer, blockRef);
        if (blockInfo == null) {
            return;
        }

        // you guessed it, the chunk the block is in. the chunk stores its own coordinates
        // (as in where the chunk itself is located within the world at large)
        //
        // we need this to effectively just add its coordinates to our block
        // -> block local coords + chunk coords ~= global position
        var worldChunk = BlockUtils.getWorldChunk(commandBuffer, blockInfo);
        if (worldChunk == null) {
            return;
        }

        // the world our chunk is in
        // the best way i've found so far to get the world that the entity is in
        // is to go
        // blockEntityRef --(has)-> blockInfo --(is in)-> worldChunk --(is in)-> world
        var world = worldChunk.getWorld();
        if (world == null) {
            return;
        }
        var coords = BlockUtils.getGlobalCoords(worldChunk, blockInfo);

        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        var component = BlockUtils.getComponent(this.tickingComponentType, commandBuffer, blockRef);
        try {
            // and call the tick method you defined on your component, which,
            // i know is sort of heresy for ECS systems, but, it makes doing
            // easy things easy. and i'm all for that
            component.onTick(
                world,
                worldChunk,
                commandBuffer,
                coords.x,
                coords.y,
                coords.z,
                worldChunk.getBlock(coords)
            );
        } catch (Throwable e) {
            console.log(String.format("ERROR: Failed to tick block at (%d, %d, %d)", coords.x, coords.y, coords.z));
            return;
        }
    }

    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }

    public void registerTo(ModPlugin plugin) {
        plugin.getChunkStoreRegistry().registerSystem(this);
    }
}
