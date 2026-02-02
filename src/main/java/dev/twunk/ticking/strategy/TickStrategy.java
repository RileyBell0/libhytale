package dev.twunk.ticking.strategy;

import javax.annotation.Nonnull;

public abstract class TickStrategy {

    @Nonnull
    public static TickStrategy always() {
        return new TickStrategyFrequency();
    }
}