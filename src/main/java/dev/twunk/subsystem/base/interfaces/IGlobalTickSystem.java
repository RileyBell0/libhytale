package dev.twunk.subsystem.base.interfaces;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.IRegistryProvider;
import dev.twunk.interfaces.methods.IQuery;
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
 */
public interface IGlobalTickSystem<
    ECS_STORE extends WorldProvider
> extends IQuery<ECS_STORE>, IRegistryProvider<ECS_STORE> {
    public void onSystemTick(
        float dt,
        @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );
}
