package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.system.IOnAddRemoveSystem;
import dev.twunk.interfaces.methods.IOnLifetime;
import dev.twunk.lib.system.AutoBlockLifetimeSystem;

/**
 * A component that implements `IEntityLifetime`
 * - It has `onEntityAdded` and `onEntityRemove` methods
 *
 * My code
 * @see IOnLifetime              - Methods for listening to entity add/remove events
 * @see IOnAddRemoveSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 * @see AutoBlockLifetimeSystem - A system I wrote that uses this. Runs onEntityAdded and onEntityRemove
 *                                     for a given query
 *
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface IOnAddRemoveComponent<
    ECS_STORE extends WorldProvider
> extends IOnLifetime<ECS_STORE>, Component<ECS_STORE> {}
