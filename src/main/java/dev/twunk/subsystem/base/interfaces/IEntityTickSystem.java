package dev.twunk.subsystem.base.interfaces;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.methods.IQuery;
import javax.annotation.Nonnull;

/**
 * Gives your system the event handler function it needs to listen/react to
 * ticks for every entity that matches your query
 *
 * When you want your system to benefit from EntityTickSubSystem
 * - implement IEntityTickSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 */
public interface IEntityTickSystem<ECS_STORE extends WorldProvider> extends IQuery<ECS_STORE> {
    public void onEntityTick(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );
}
