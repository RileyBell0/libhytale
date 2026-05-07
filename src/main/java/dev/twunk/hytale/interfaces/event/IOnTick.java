package dev.twunk.hytale.interfaces.event;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.interfaces.config.IEventConfig;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.ref.AnyRef;

/// My code
/// @see OnTick - SubSystem that runs IEntityTick methods via IEntityTickSystem(s)
///
/// Hytale's code
/// @see EntityTickingSystem - Baseline hytale system for ticking entities.
///                            It's the underlying driver of IEntityTickSubSystem
public interface IOnTick<ECS_TYPE extends WorldProvider> extends IEventConfig<ECS_TYPE>, IQuery<ECS_TYPE> {
    void onTick(float dt, AnyRef<ECS_TYPE> ref, CommandBuffer<ECS_TYPE> commandBuffer);
}
