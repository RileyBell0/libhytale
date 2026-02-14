package dev.twunk.system.composite;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.system.response.TickResponse;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Gives your system the event handler function it needs to run code for every entity
 * that matches your system, AND for that entity to be able to sleep etc (and remember
 * if it was awake/sleeping etc when it loads back in)
 *
 * When you want your system to benefit from ScheduledTickSubSystem
 * - implement IScheduledTickSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 */
public interface IScheduledTickSystem {
    @Nullable
    public abstract TickResponse onEntityTick(
        @Nonnull World world,
        @Nonnull WorldChunk chunk,
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull Vector3i globalCoords,
        int blockId,
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
