package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.methods.IEntityLifetime;

/**
 * A component that implements `IEntityLifetime`
 * - It has `onEntityAdded` and `onEntityRemove` methods
 *
 * My code
 * @see dev.twunk.interfaces.methods.IEntityLifetime                My subsystem methods/interface
 *
 * @see dev.twunk.subsystem.base.interfaces.IEntityLifetimeSystem   Additional requirements that an implementor of IEntityLifetime must satisfy
 *                                                                  in order to register a subsystem to run itself
 *
 * @see dev.twunk.subsystem.base.EntityLifetimeSubSystem            The base subsystem that "runs" something with "IEntityLifetime"
 *
 * @see dev.twunk.system.BlockLifetimeComponentSystem               A system I wrote that uses this
 *
 *
 * Hytale's code
 * @see com.hypixel.hytale.component.system.RefSystem   Hytale's underlying system that powers it (kind of)
 */
public interface IBlockLifetimeComponent extends IEntityLifetime<ChunkStore>, Component<ChunkStore> {}
