package dev.twunk.system.interfaces;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.system.response.TickResponse;
import dev.twunk.system.smart.TickingEntityMetadata;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IScheduledTickSystem {
    @Nullable
    public abstract TickResponse onEntityTick(
        @Nonnull TickingEntityMetadata state,
        float dt,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    );

    /**
     * Must provide a STABLE id. this is what your ticking state is stored against
     * for each and every entitiy you match. if you change this, all previous
     * ticking state will be lost forever
     *
     * WARNING: it will still be stored on the entities. There is no cleanup planned
     * for leftover system ticking information - and there never should be. Ideally
     * if you
     *  1) play with your mod, then
     *  2) uninstall your mod, then
     *  3) play without it, then
     *  4) reinstall it
     * your entities will still remember what they were up to in your mod.
     *
     * This is intentional. If you don't want that to happen, you're welcome
     * to clear out all old ticking data manually for JUST your mod, but be careful
     *
     * Note: you're welcome to clean out old entries yourself
     *
     * Do not change the ID you chose unless you're REALLY REALLY sure you
     * want this.
     */
    public String getId();
}
