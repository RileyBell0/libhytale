package dev.twunk.interfaces.component.auto;

import dev.twunk.interfaces.component.IBlockLifetimeComponent;

/**
 * Any component that implements this (that you register via my lib code) will
 * have a system automatically spun up for it to run the onEntityAdded and
 * onEntityRemove events
 *
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
 * Hytale's code
 * @see com.hypixel.hytale.component.system.RefSystem Hytale's underlying system that powers it (kind of)
 */
public interface IAutoBlockLifetimeComponent extends IBlockLifetimeComponent {}
