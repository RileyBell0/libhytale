package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.refs.BlockRef;
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

    public final class ForListener extends OnBlockTick {

        private final IOnBlockTick listener;

        protected ForListener(IOnBlockTick listener, Query<ChunkStore> query) {
            super(query);
            this.listener = listener;
        }

        /**
         * Shim around other method for reducing boilerplate if i define a query on my class
         */
        public static final <T extends IOnBlockTick & IQuery<ChunkStore>> OnBlockTick newUninitialised(T listener) {
            return newUninitialised(listener, listener.getQuery());
        }

        public static final OnBlockTick newUninitialised(IOnBlockTick listener, Query<ChunkStore> query) {
            return IEventDriver.__construct(
                IEventDriver.__dupeClassAndGetConstructor(
                    OnBlockTick.ForListener.class,
                    IOnBlockTick.class,
                    Query.class
                ),
                listener,
                query
            );
        }

        ///////////////////////////////////////////////////////////////////////////
        // \/======================\/-  Methods  -\/==========================\/ //
        ///////////////////////////////////////////////////////////////////////////

        public final void tick(
            float dt,
            int index,
            ArchetypeChunk<ChunkStore> archetypeChunk,
            Store<ChunkStore> store,
            CommandBuffer<ChunkStore> commandBuffer
        ) {
            listener.onBlockTick(new BlockRef(archetypeChunk.getReferenceTo(index)), commandBuffer);
        }
    }

    public final class ForComponent<T extends Component<ChunkStore>> extends OnBlockTick {

        private final ComponentType<ChunkStore, T> componentType;

        /**
         * Bound for T fully defined here
         */
        public static <T extends IOnBlockTick & Component<ChunkStore>> OnBlockTick init(
            ComponentType<ChunkStore, T> componentType
        ) {
            return IEventDriver.__construct(
                IEventDriver.__dupeClassAndGetConstructor(OnBlockTick.ForComponent.class, ComponentType.class),
                componentType
            );
        }

        protected ForComponent(ComponentType<ChunkStore, T> componentType) {
            super(Query.and(componentType));
            this.componentType = componentType;
        }

        ///////////////////////////////////////////////////////////////////////////
        // \/======================\/-  Methods  -\/==========================\/ //
        ///////////////////////////////////////////////////////////////////////////

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
}
