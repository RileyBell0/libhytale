package dev.twunk.interfaces.component.auto;

import com.hypixel.hytale.component.system.RefSystem;
import dev.twunk.interfaces.component.IBlockLifetimeComponent;
import dev.twunk.interfaces.methods.IEntityLifetime;
import dev.twunk.interfaces.subsystem.IEntityLifetimeSystem;
import dev.twunk.lib.system.AutoBlockLifetimeSystem;
import dev.twunk.subsystem.base.EntityLifetimeSubSystem;

/**
 * Any component that implements this (that you register via my lib code) will
 * have a system automatically spun up for it to run the onEntityAdded and
 * onEntityRemove events
 *
 *
 * My code
 * @see IEntityLifetime              - Methods for listening to entity add/remove events
 * @see IEntityLifetimeSystem        - Additional requirements that an implementor of IEntityLifetime must satisfy
 *                                     in order to register a subsystem to run itself
 * @see EntityLifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 * @see AutoBlockLifetimeSystem - A system I wrote that uses this. Runs onEntityAdded and onEntityRemove
 *                                     for a given query
 *
 * Hytale's code
 * @see RefSystem -Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface IAutoBlockLifetimeComponent extends IBlockLifetimeComponent {}
