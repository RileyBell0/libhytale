package dev.twunk.system.composite;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.IRegisteredComponent;
import dev.twunk.system.SubSystemOwner;
import dev.twunk.system.interfaces.IEntityTickSystem;
import dev.twunk.system.interfaces.ITickableBlockEntitySystem;
import dev.twunk.utils.BlockUtils;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * Intended for re-use as is. Also intended for minimal usage (mainly testing etc)
 *
 * GOAL: Tick ALL block entities that have the provided component
 *
 *
 */
public class QueryTickingSubSystem extends SubSystemOwner implements IEntityTickSystem {

    private final @Nonnull ITickableBlockEntitySystem parent;

    public QueryTickingSubSystem(
        @Nonnull final ITickableBlockEntitySystem parent,
        @Nonnull final Query<ChunkStore> query
    ) {
        super(query);
        this.parent = parent;
    }

    public QueryTickingSubSystem(
        @Nonnull final ITickableBlockEntitySystem parent,
        @Nonnull Supplier<ComponentType<ChunkStore, ? extends IRegisteredComponent>> supplier
    ) {
        super(Query.and(supplier.get()));
        this.parent = parent;
    }

    public QueryTickingSubSystem(
        @Nonnull final ITickableBlockEntitySystem parent,
        @Nonnull Class<? extends IRegisteredComponent> componentClass
    ) {
        super(Query.and(IRegisteredComponent.getComponentType(componentClass)));
        this.parent = parent;
    }

    public QueryTickingSubSystem(
        @Nonnull final ITickableBlockEntitySystem parent,
        @Nonnull ComponentType<ChunkStore, ? extends IRegisteredComponent> tickingComponentType
    ) {
        super(Query.and(tickingComponentType));
        this.parent = parent;
    }

    public void onEntityTick(
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

        parent.onBlockEntityTick(world, worldChunk, commandBuffer, coords, worldChunk.getBlock(coords));
    }
}
