package dev.twunk.hytale.interfaces.subsystem;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IRegistryProvider;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.lib.TickPlan;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO really not working that well atm, needs to be cleaned up BUT it worked
 * well in concept and practice. Just, yeah, needs to be cleaned up and extended to support
 * more advanced ticking ideas like "every x ticks" or "every x seconds" etc etc
 *
 * Gives your system the event handler function it needs to run code for every entity
 * that matches your system, AND for that entity to be able to sleep etc (and remember
 * if it was awake/sleeping etc when it loads back in)
 *
 * When you want your system to benefit from ScheduledTickSubSystem
 * - implement IScheduledTickSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 */
public interface IScheduledTickSystem<
    ECS_STORE extends WorldProvider
> extends IQuery<ECS_STORE>, IRegistryProvider<ECS_STORE> {
    @Nullable
    public abstract TickPlan onEntityTick(
        final @Nonnull World world,
        final @Nonnull Ref<ECS_STORE> ref,
        final float dt,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
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
    @Nonnull
    public String getId();
}
