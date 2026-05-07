package dev.twunk.hytale.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.ISystemEventDriver;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.interfaces.event.IOnBlockTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.BlockRef;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/// Composite subsystem to allow the parent to run code on its elements every
/// tick in a smarter way
///
/// GOAL: tick all block entities that match the given query.
///
/// REQUIRES:
/// - EntityTickSubSystem -> allows us to tick all blocks that match our query
/// PRODUCES:
/// - IQueryTickingSystem runner
///
/// @see OnTick - BlockTickSubSystem is simply an extension of EntityTickSubSystem
///                            that grabs some more block-related data out of a ref before calling
///                            the onBlockTick method your `IEntityTickSystem` provides
/// @see IOnBlockTick      - method i'll be calling on your class
public abstract class OnBlockTick
        extends EntityTickingSystem<ChunkStore> // EntityTickingSystem is hytale's underlying code that powers this
        implements ISystemEventDriver<ChunkStore> {

    private Set<Dependency<ChunkStore>> dependencies = new HashSet<>();

    @Nullable
    private SystemGroup<ChunkStore> group = null;

    private final IOnBlockTick listener;
    private final Query<ChunkStore> query;
    private final IRegistry<ChunkStore> registry;

    protected OnBlockTick(Query<ChunkStore> query, IOnBlockTick listener) {
        this.registry = LibHytale.CHUNK_REGISTRY;
        this.query = query;
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    public final void tick(
            float dt,
            int index,
            ArchetypeChunk<ChunkStore> archetypeChunk,
            Store<ChunkStore> store,
            CommandBuffer<ChunkStore> commandBuffer
    ) {
        listener.onBlockTick(new BlockRef(archetypeChunk.getReferenceTo(index)), commandBuffer);
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Getters/setters  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region getters/setters

    @Override
    public Set<Dependency<ChunkStore>> getDependencies() {
        return this.dependencies;
    }

    @Override
    public void setDependencies(Set<Dependency<ChunkStore>> dependencies) {
        this.dependencies = new HashSet<>();
        this.dependencies.addAll(dependencies);
    }

    @Override
    public boolean addDependency(Dependency<ChunkStore> dependency) {
        return this.dependencies.add(dependency);
    }

    @Override
    @Nullable
    public SystemGroup<ChunkStore> getGroup() {
        return this.group;
    }

    public void setGroup(@Nullable SystemGroup<ChunkStore> group) {
        this.group = group;
    }

    @Override
    public final Query<ChunkStore> getQuery() {
        return this.query;
    }

    @Override
    public final IRegistry<ChunkStore> getRegistry() {
        return this.registry;
    }

    // #endregion getters/setters

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Shim around other method for reducing boilerplate if i define a query on my class
     */
    public static <T extends IOnBlockTick & IQuery<ChunkStore>> OnBlockTick newDriverFor(T listener) {
        return newDriverFor(listener.getQuery(IOnBlockTick.class), listener);
    }

    public static OnBlockTick newDriverFor(IQuery<ChunkStore> queryProvider, IOnBlockTick listener) {
        return newDriverFor(queryProvider.getQuery(IOnBlockTick.class), listener);
    }

    public static OnBlockTick newDriverFor(Query<ChunkStore> query, IOnBlockTick listener) {
        return IEventDriver.__construct(
                IEventDriver.__dupeClassAndGetConstructor(OnBlockTick.class, Query.class, IOnBlockTick.class),
                query,
                listener
        );
    }
}
