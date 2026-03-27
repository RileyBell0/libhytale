package dev.twunk.subsystem.base.interfaces;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.IRegistryProvider;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.subsystem.base.GlobalTickSubSystem;
import javax.annotation.Nonnull;

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
 * @see GlobalTickSubSystem    - runs IGlobalTickSystem implementors (given that the implementors
 *                               themselves load a GlobalTickSubSystem for themselves)
 *
 * Hytale's code
 * @see ArchetypeTickingSystem - I use this to run GlobalTickSubSystem(s). Only way i currently know
 *                               of for getting a commandBuffer in a global tick
 */
public interface IGlobalTickSystem<
    ECS_STORE extends WorldProvider
> extends IQuery<ECS_STORE>, IRegistryProvider<ECS_STORE> {
    public void onSystemTick(
        final float dt,
        final @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );
}
