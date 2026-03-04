package dev.twunk.component;

/**
 * Same as ITickableBlockComponent except if you implement THIS version on a component it'll
 * automatically get a system generated for it when you register your component that has this attached
 */
public interface IAutoTickingBlockComponent extends ITickableBlockComponent {}
