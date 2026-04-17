package dev.twunk.interfaces.events;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.system.OnTick;

/**
 * My code
 * @see OnTick - SubSystem that runs IEntityTick methods via IEntityTickSystem(s)
 *
 * Hytale's code
 * @see EntityTickingSystem - Baseline hytale system for ticking entities.
 *                            It's the underlying driver of IEntityTickSubSystem
 */
@FunctionalInterface
public interface IOnTick<ECS_TYPE extends WorldProvider> {
    public void onTick(final float dt, final AnyRef<ECS_TYPE> ref, final CommandBuffer<ECS_TYPE> commandBuffer);
}
