package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.methods.IBlockTick;
import dev.twunk.interfaces.methods.IEntityTick;
import dev.twunk.interfaces.subsystem.IEntityTickSystem;
import dev.twunk.lib.system.AutoBlockTickSystem;
import dev.twunk.subsystem.base.EntityTickSubSystem;

/**
 * A component that implements `IBlockTick`
 * - It has an `onBlockTick` method
 *
 * My code
 * @see AutoBlockTickSystem - System for specifically ticking block entities (gets
 *                                     block position, id, etc. then gives that to your tick method)
 * @see IBlockTick                   - Definition of method for ticking a block entity
 * @see EntityTickSubSystem          - SubSystem used by TickableBlockComponentSystem
 *                                     that ticks entities (in our case block entities)
 * @see IEntityTickSystem            - Something that can be ticked by EntityTickSubSystem
 *                                     (satisfies IEntityTick)
 * @see IEntityTick                  - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per block, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public interface ITickableBlockComponent extends IBlockTick, Component<ChunkStore> {}
