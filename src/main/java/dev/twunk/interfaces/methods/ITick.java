package dev.twunk.interfaces.methods;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.system.TickSubSystem;
import dev.twunk.interfaces.subsystem.ITickSystem;
import javax.annotation.Nonnull;

/**
 * My code
 * @see TickSubSystem - SubSystem that runs IEntityTick methods via IEntityTickSystem(s)
 * @see ITickSystem   - Something that can be ticked by EntityTickSubSystem
 *                            (satisfies IEntityTick)
 *
 * Hytale's code
 * @see EntityTickingSystem - Baseline hytale system for ticking entities.
 *                            It's the underlying driver of IEntityTickSubSystem
 */
public interface ITick<ECS_STORE extends WorldProvider> {
    public void onEntityTick(
        final float dt,
        final @Nonnull AnyRef<ECS_STORE> ref,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );
}
