package dev.twunk.hytale.interfaces.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.interfaces.config.IEventConfig;
import dev.twunk.hytale.interfaces.config.IQuery;

/**
 * My code
 * @see OnTick - SubSystem that runs IEntityTick methods via IEntityTickSystem(s)
 *
 * Hytale's code
 * @see EntityTickingSystem - Baseline hytale system for ticking entities.
 *                            It's the underlying driver of IEntityTickSubSystem
 */
public interface IOnWorldTick<ECS_TYPE extends WorldProvider> extends IEventConfig<ECS_TYPE>, IQuery<ECS_TYPE> {
    public void onWorldTick(
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    );
}
