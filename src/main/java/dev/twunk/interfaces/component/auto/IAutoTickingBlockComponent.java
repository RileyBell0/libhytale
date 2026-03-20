package dev.twunk.interfaces.component.auto;

import dev.twunk.interfaces.component.ITickableBlockComponent;

/**
 * A component that has an onBlockTick method that when registered will AUTOMATICALLY
 * have a system registered for it
 *
 * TLDR: Put this on your block, and it'll have its onBlockTick method called
 * automatically every tick with zero configuration
 */
public interface IAutoTickingBlockComponent extends ITickableBlockComponent {}
