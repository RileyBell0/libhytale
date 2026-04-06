package dev.twunk.hytale.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.refs.BlockRef;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IBlockTick;
import dev.twunk.interfaces.methods.IRegistry;
import dev.twunk.interfaces.methods.ITick;

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
 * @see TickSubSystem - BlockTickSubSystem is simply an extension of EntityTickSubSystem
 *                            that grabs some more block-related data out of a ref before calling
 *                            the onBlockTick method your `IEntityTickSystem` provides
 * @see IBlockTick          - method i'll be calling on your class
 */
public class BlockTickSubSystem
    extends SubSystemOwner<ChunkStore>
    implements ITick<ChunkStore>, ISubSystem<ChunkStore>
{

    private final IBlockTick listener;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <T extends BlockTickSubSystem> BlockTickSubSystem newSubsystemFor(
        final IBlockTick listener,
        final Query<ChunkStore> query
    ) {
        return ISubSystem.__newSubSystem(BlockTickSubSystem.class, IBlockTick.class, listener, query);
    }

    protected BlockTickSubSystem(final IBlockTick listener, final Query<ChunkStore> query) {
        super(query);
        this.listener = listener;

        this.appendSubSystem(TickSubSystem.newSubsystemFor(this, query, this.getRegistry()));
    }

    public void onEntityTick(
        final float dt,
        final AnyRef<ChunkStore> ref,
        final CommandBuffer<ChunkStore> commandBuffer
    ) {
        listener.onBlockTick(new BlockRef(ref), commandBuffer);
    }

    @Override
    public IRegistry<ChunkStore> getRegistry() {
        return LibHytale.CHUNK_REGISTRY;
    }
}
