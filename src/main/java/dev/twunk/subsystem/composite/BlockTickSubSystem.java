package dev.twunk.subsystem.composite;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.TwunkLib;
import dev.twunk.interfaces.IRegistry;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IBlockTick;
import dev.twunk.interfaces.subsystem.IBlockTickSystem;
import dev.twunk.interfaces.subsystem.IEntityTickSystem;
import dev.twunk.subsystem.SubSystemOwner;
import dev.twunk.subsystem.base.EntityTickSubSystem;
import dev.twunk.utils.BlockUtils;
import javax.annotation.Nonnull;

/**
 * Composite subsystem to allow the parent to run code on its elements every
 * tick in a smarter way
 *
 * GOAL: tick all block entities that match the given query.
 *
 * REQUIRES:
 * - EntityTickSubSystem -> allows us to tick all blocks that match our query
 * PRODUCES:
 * - IQueryTickingSystem runner
 *
 * @see EntityTickSubSystem - BlockTickSubSystem is simply an extension of EntityTickSubSystem
 *                            that grabs some more block-related data out of a ref before calling
 *                            the onBlockTick method your `IEntityTickSystem` provides
 * @see IEntityTickSystem   - Something that EntityTickSubSystem can run (this)
 * @see IBlockTick          - method i'll be calling on your class
 * @see IBlockTickSystem    - your class must implement this. It will have an IBlockTick method
 *                            that this sub system is going to be calling on it
 */
public class BlockTickSubSystem
    extends SubSystemOwner<ChunkStore>
    implements IEntityTickSystem<ChunkStore>, ISubSystem<ChunkStore>
{

    private final @Nonnull IBlockTickSystem parent;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <T extends BlockTickSubSystem> BlockTickSubSystem newSubsystemFor(
        final @Nonnull IBlockTickSystem parent
    ) {
        return ISubSystem.__newSubSystem(BlockTickSubSystem.class, IBlockTickSystem.class, parent);
    }

    protected BlockTickSubSystem(final @Nonnull IBlockTickSystem parent) {
        super(parent.getQuery());
        this.parent = parent;

        this.appendSubSystem(EntityTickSubSystem.newSubsystemFor(this));
    }

    public void onEntityTick(
        final float dt,
        final int index,
        final @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        final @Nonnull Store<ChunkStore> store,
        final @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // blockEntityRef --(has)-> blockInfo --(is in)-> worldChunk --(is in)-> world
        // blockInfo has local coords
        // blockInfo & worldChunk --(implies)-> global coords

        // ref to our block entity (ref to the entity at the current
        // index that matches the query - imagine the index as an arbtrary
        // `i` in a for loop. think nothing of it)
        final var blockRef = archetypeChunk.getReferenceTo(index);

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
        final var blockInfo = dev.twunk.utils.BlockUtils.Info.get(blockRef);
        if (blockInfo == null) {
            return;
        }

        // you guessed it, the chunk the block is in. the chunk stores its own coordinates
        // (as in where the chunk itself is located within the world at large)
        //
        // we need this to effectively just add its coordinates to our block
        // -> block local coords + chunk coords ~= global position
        final var worldChunk = dev.twunk.utils.ChunkUtils.WorldChunk_.get(blockInfo);
        if (worldChunk == null) {
            return;
        }

        // the world our chunk is in
        // the best way i've found so far to get the world that the entity is in
        // is to go
        // blockEntityRef --(has)-> blockInfo --(is in)-> worldChunk --(is in)-> world
        final var world = worldChunk.getWorld();
        if (world == null) {
            return;
        }
        final var coords = BlockUtils.Coords.Global.get(worldChunk, blockInfo);

        parent.onBlockTick(blockRef, world, worldChunk, commandBuffer, coords, worldChunk.getBlock(coords));
    }

    @Override
    public IRegistry<ChunkStore> getRegistry() {
        return TwunkLib.CHUNK_REGISTRY;
    }
}
