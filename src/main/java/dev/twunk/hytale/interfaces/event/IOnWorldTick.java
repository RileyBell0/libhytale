package dev.twunk.hytale.interfaces.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.methods.IQuery;

/**
 * Gives your system the event handler function it needs to run some code every
 * tick (once per tick, not per entity. just, once. e.g. this will run 30x per second
 * overall. is that clear? as mud? fuck.)
 *
 * When you want your system to benefit from GlobalTickSubSystem
 * - implement IGlobalTickSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 *
 * My code
 * @see OnUniverseTickEventDriver    - runs IGlobalTickSystem implementors (given that the implementors
 *                               themselves load a GlobalTickSubSystem for themselves)
 *
 * Hytale's code
 * @see ArchetypeTickingSystem - I use this to run GlobalTickSubSystem(s). Only way i currently know
 *                               of for getting a commandBuffer in a global tick
 */
@FunctionalInterface
public interface IOnWorldTick<ECS_TYPE extends WorldProvider> {
    public void onWorldTick(
        final float dt,
        final ArchetypeChunk<ECS_TYPE> archetypeChunk,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
    );

    public interface IOnWorldTick__IQuery<
        ECS_TYPE extends WorldProvider
    > extends IOnWorldTick<ECS_TYPE>, IQuery<ECS_TYPE> {}
}
