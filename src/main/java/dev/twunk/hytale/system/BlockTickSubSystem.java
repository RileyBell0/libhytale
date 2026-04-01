package dev.twunk.hytale.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.refs.BlockRef;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IBlockTick;
import dev.twunk.interfaces.methods.IRegistry;
import dev.twunk.interfaces.subsystem.IBlockTickSystem;
import dev.twunk.interfaces.subsystem.ITickSystem;
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
 * @see TickSubSystem - BlockTickSubSystem is simply an extension of EntityTickSubSystem
 *                            that grabs some more block-related data out of a ref before calling
 *                            the onBlockTick method your `IEntityTickSystem` provides
 * @see ITickSystem   - Something that EntityTickSubSystem can run (this)
 * @see IBlockTick          - method i'll be calling on your class
 * @see IBlockTickSystem    - your class must implement this. It will have an IBlockTick method
 *                            that this sub system is going to be calling on it
 */
public class BlockTickSubSystem
    extends SubSystemOwner<ChunkStore>
    implements ITickSystem<ChunkStore>, ISubSystem<ChunkStore>
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

        this.appendSubSystem(TickSubSystem.newSubsystemFor(this));
    }

    public void onEntityTick(
        final float dt,
        final @Nonnull AnyRef<ChunkStore> ref,
        final @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        parent.onBlockTick(new BlockRef(ref), commandBuffer);
    }

    @Override
    public IRegistry<ChunkStore> getRegistry() {
        return LibHytale.CHUNK_REGISTRY;
    }
}
