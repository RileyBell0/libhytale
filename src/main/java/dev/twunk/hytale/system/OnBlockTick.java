package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.system.ignoreme.OnBlockTick__Component;
import dev.twunk.hytale.system.ignoreme.OnBlockTick__Listener;
import dev.twunk.interfaces.IEventDriver;
import dev.twunk.interfaces.events.IOnBlockTick;
import dev.twunk.interfaces.methods.IQuery;

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
 * @see OnTick - BlockTickSubSystem is simply an extension of EntityTickSubSystem
 *                            that grabs some more block-related data out of a ref before calling
 *                            the onBlockTick method your `IEntityTickSystem` provides
 * @see IOnBlockTick      - method i'll be calling on your class
 */
public abstract class OnBlockTick extends OnTick<ChunkStore> {

    protected OnBlockTick(Query<ChunkStore> query) {
        super(query, LibHytale.CHUNK_REGISTRY);
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public abstract void tick(
        float dt,
        int index,
        ArchetypeChunk<ChunkStore> archetypeChunk,
        Store<ChunkStore> store,
        CommandBuffer<ChunkStore> commandBuffer
    );

    ///////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Shim around other method for reducing boilerplate if i define a query on my class
     */
    public static final <T extends IOnBlockTick & IQuery<ChunkStore>> OnBlockTick newUninitialised(T listener) {
        return newUninitialised(listener, listener.getQuery());
    }

    public static final OnBlockTick newUninitialised(IOnBlockTick listener, Query<ChunkStore> query) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(OnBlockTick__Listener.class, IOnBlockTick.class, Query.class),
            listener,
            query
        );
    }

    /**
     * Bound for T fully defined here
     */
    public static final <T extends Component<ChunkStore>> OnBlockTick newUninitialised(
        ComponentType<ChunkStore, T> componentType
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(OnBlockTick__Component.class, ComponentType.class),
            componentType
        );
    }
}
