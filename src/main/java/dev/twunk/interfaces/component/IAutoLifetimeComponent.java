package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.system.LifetimeSubSystem;
import dev.twunk.interfaces.methods.ILifetime;
import dev.twunk.interfaces.subsystem.ILifetimeSystem;
import dev.twunk.lib.system.AutoBlockLifetimeSystem;

/**
 * Any component that implements this (that you register via my lib code) will
 * have a system automatically spun up for it to run the onEntityAdded and
 * onEntityRemove events
 *
 *
 * My code
 * @see ILifetime              - Methods for listening to entity add/remove events
 * @see ILifetimeSystem        - Additional requirements that an implementor of IEntityLifetime must satisfy
 *                                     in order to register a subsystem to run itself
 * @see LifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 * @see AutoBlockLifetimeSystem - A system I wrote that uses this. Runs onEntityAdded and onEntityRemove
 *                                     for a given query
 *
 * Hytale's code
 * @see RefSystem -Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface IAutoLifetimeComponent<ECS_STORE extends WorldProvider> extends ILifetimeComponent<ECS_STORE> {}
