package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.methods.IEntityLifetime;
import dev.twunk.interfaces.subsystem.IEntityLifetimeSystem;
import dev.twunk.lib.system.AutoBlockLifetimeSystem;
import dev.twunk.subsystem.base.EntityLifetimeSubSystem;

/**
 * A component that implements `IEntityLifetime`
 * - It has `onEntityAdded` and `onEntityRemove` methods
 *
 * My code
 * @see IEntityLifetime              - Methods for listening to entity add/remove events
 * @see IEntityLifetimeSystem        - Additional requirements that an implementor of IEntityLifetime must satisfy
 *                                     in order to register a subsystem to run itself
 * @see EntityLifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 * @see AutoBlockLifetimeSystem - A system I wrote that uses this. Runs onEntityAdded and onEntityRemove
 *                                     for a given query
 *
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface IBlockLifetimeComponent extends IEntityLifetime<ChunkStore>, Component<ChunkStore> {}
