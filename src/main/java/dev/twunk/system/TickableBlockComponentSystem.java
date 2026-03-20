package dev.twunk.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.ITickableBlockComponent;
import dev.twunk.subsystem.SubSystemOwner;
import dev.twunk.subsystem.base.EntityTickSubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityTickSystem;
import dev.twunk.utils.TwunkLib;
import dev.twunk.utils.world.Utils;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * Intended for re-use as is. Also intended for minimal usage (mainly testing etc)
 *
 * GOAL: Tick ALL block entities that have the provided component
 *
 * Marked as final since, this is really a one and done sorta deal, just clone
 * its src and edit if you need alterations, because, then its just not this
 * specific thing anymore.
 *
 * How to use:
 * - create a new instance `new TickableBlockComponent<YourComponent>(YourComponentType)`
 * - register the instance to your plugin
 */
public final class TickableBlockComponentSystem<T extends ITickableBlockComponent>
    extends SubSystemOwner
    implements IEntityTickSystem
{

    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    private final @Nonnull ComponentType<ChunkStore, T> componentType;

    public TickableBlockComponentSystem(@Nonnull Supplier<ComponentType<ChunkStore, T>> supplier) {
        super(Query.and(supplier.get()));
        var component = supplier.get();
        if (component == null) {
            throw new RuntimeException("Failed to get component type for Component Ticking System | " + supplier);
        }
        this.componentType = component;
        this.appendSubSystem(EntityTickSubSystem.newSubsystemFor(this));
    }

    public TickableBlockComponentSystem(@Nonnull Class<T> componentClass) {
        super(Query.and(TwunkLib.getComponentType(componentClass)));
        this.componentType = TwunkLib.getComponentType(componentClass);
        this.appendSubSystem(EntityTickSubSystem.newSubsystemFor(this));
    }

    public TickableBlockComponentSystem(@Nonnull ComponentType<ChunkStore, T> componentType) {
        super(Query.and(componentType));
        this.componentType = componentType;
        this.appendSubSystem(EntityTickSubSystem.newSubsystemFor(this));
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
        var blockInfo = Utils.Block.Info.get(blockRef);
        if (blockInfo == null) {
            return;
        }

        // you guessed it, the chunk the block is in. the chunk stores its own coordinates
        // (as in where the chunk itself is located within the world at large)
        //
        // we need this to effectively just add its coordinates to our block
        // -> block local coords + chunk coords ~= global position
        var worldChunk = Utils.Chunk.WorldChunk_.get(blockInfo);
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
        var coords = Utils.Block.Coords.Global.get(worldChunk, blockInfo);

        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        var component = Utils.Component_.get(blockRef, this.componentType);
        try {
            // and call the tick method you defined on your component, which,
            // i know is sort of heresy for ECS systems, but, it makes doing
            // easy things easy. and i'm all for that
            component.onBlockTick(blockRef, world, worldChunk, commandBuffer, coords, worldChunk.getBlock(coords));
        } catch (Throwable e) {
            console.log(String.format("ERROR: Failed to tick block at (%d, %d, %d)", coords.x, coords.y, coords.z));
            return;
        }
    }
}
