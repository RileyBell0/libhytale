package dev.twunk.interfaces.methods;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.subsystem.base.EntityTickSubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityTickSystem;
import javax.annotation.Nonnull;

/**
 * My code
 * @see EntityTickSubSystem - SubSystem that runs IEntityTick methods via IEntityTickSystem(s)
 * @see IEntityTickSystem   - Something that can be ticked by EntityTickSubSystem
 *                            (satisfies IEntityTick)
 *
 * Hytale's code
 * @see EntityTickingSystem - Baseline hytale system for ticking entities.
 *                            It's the underlying driver of IEntityTickSubSystem
 */
public interface IEntityTick<ECS_STORE extends WorldProvider> {
    public void onEntityTick(
        final float dt,
        final int index,
        final @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );
}
