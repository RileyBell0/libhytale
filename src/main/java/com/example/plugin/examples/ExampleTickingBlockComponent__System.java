package com.example.plugin.examples;

import com.example.plugin.interfaces.TickingBlockComponent_System;

/**
 * If you have a block you want to tick, add a component to it and tick that instead
 *
 * If you have a component you want to tick, simply
 * - implement `TickingBlockComponent` on your class
 * - add it to `TickingInitialiser`
 */
public class ExampleTickingBlockComponent__System extends TickingBlockComponent_System<ExampleTickingBlockComponent> {

    public ExampleTickingBlockComponent__System() {
        super(ExampleTickingBlockComponent::getComponentType);
    }
}
