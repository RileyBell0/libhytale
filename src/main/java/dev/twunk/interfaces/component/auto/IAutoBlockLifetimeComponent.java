package dev.twunk.interfaces.component.auto;

import dev.twunk.interfaces.component.IBlockLifetimeComponent;

/**
 * Any component that implements this (that you register via my lib code) will
 * have a system automatically spun up for it to run the onEntityAdded and
 * onEntityRemove events
 */
public interface IAutoBlockLifetimeComponent extends IBlockLifetimeComponent {}
