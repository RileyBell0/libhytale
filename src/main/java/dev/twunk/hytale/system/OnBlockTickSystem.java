package dev.twunk.hytale.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.refs.BlockRef;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IOnBlockTick;
import dev.twunk.interfaces.methods.IOnTick;
import dev.twunk.interfaces.methods.IRegistry;

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
 * @see OnTickSystem - BlockTickSubSystem is simply an extension of EntityTickSubSystem
 *                            that grabs some more block-related data out of a ref before calling
 *                            the onBlockTick method your `IEntityTickSystem` provides
 * @see IOnBlockTick          - method i'll be calling on your class
 */
public class OnBlockTickSystem
    extends SubSystemOwner<ChunkStore>
    implements IOnTick<ChunkStore>, ISubSystem<ChunkStore>
{

    private final IOnBlockTick listener;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static OnBlockTickSystem constructNewSystemClass(IOnBlockTick listener, Query<ChunkStore> query) {
        return ISubSystem.__construct(
            ISubSystem.__dupeClassAndGetConstructor(OnBlockTickSystem.class, IOnBlockTick.class, Query.class),
            listener,
            query
        );
    }

    protected OnBlockTickSystem(IOnBlockTick listener, Query<ChunkStore> query) {
        super(query);
        this.listener = listener;

        this.appendSubSystem(OnTickSystem.constructNewSystemClass(this, query, this.getRegistry()));
    }

    public void onEntityTick(float dt, AnyRef<ChunkStore> ref, CommandBuffer<ChunkStore> commandBuffer) {
        listener.onBlockTick(new BlockRef(ref), commandBuffer);
    }

    @Override
    public IRegistry<ChunkStore> getRegistry() {
        return LibHytale.CHUNK_REGISTRY;
    }
}
