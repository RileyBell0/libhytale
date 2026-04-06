package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.system.LifetimeSubSystem;
import dev.twunk.interfaces.methods.ILifetime;
import dev.twunk.lib.system.AutoBlockLifetimeSystem;

/**
 * A component that implements `IEntityLifetime`
 * - It has `onEntityAdded` and `onEntityRemove` methods
 *
 * My code
 * @see ILifetime              - Methods for listening to entity add/remove events
 * @see LifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 * @see AutoBlockLifetimeSystem - A system I wrote that uses this. Runs onEntityAdded and onEntityRemove
 *                                     for a given query
 *
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface ILifetimeComponent<
    ECS_STORE extends WorldProvider
> extends ILifetime<ECS_STORE>, Component<ECS_STORE> {}
