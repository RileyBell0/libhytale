package dev.twunk.interfaces.component.auto;

import dev.twunk.interfaces.component.ITickableBlockComponent;

/**
 * Any component that implements this (that you register via my lib code) will
 * have a system automatically spun up for it to run the onBlockTick code
 *
 * @see IBlockTick Definition of method for ticking a block
 */
public interface IAutoTickingBlockComponent extends ITickableBlockComponent {}
