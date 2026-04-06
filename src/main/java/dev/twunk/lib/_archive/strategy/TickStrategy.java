package dev.twunk.lib._archive.strategy;

public abstract class TickStrategy {

    public static TickStrategy always() {
        return new TickStrategyFrequency();
    }
}
