package dev.twunk.lib._archive.strategy;

public class TickStrategyFrequency extends TickStrategy {

    // I want to run every X ticks
    // - basically just means we want to SLEEP x seconds between each run for each
    // component, BUT, they don't necessarily need to be synchronized
    public final int tickFrequency;

    // When something starts ticking, this is how long we should DELAY until
    // its first tick gets run
    public final int initialDelay;

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    public TickStrategyFrequency() {
        // tick every single tick
        this.tickFrequency = 1;

        // tick every single tick
        this.initialDelay = 0;
    }

    public TickStrategyFrequency(final int tickFrequency) {
        this.tickFrequency = enforceFrequencyBounds(tickFrequency);
        this.initialDelay = 0;
    }

    public TickStrategyFrequency(final int tickFrequency, final int initialDelay) {
        this.tickFrequency = enforceFrequencyBounds(tickFrequency);
        this.initialDelay = enforceInitialDelayBounds(initialDelay);
    }

    private static int enforceFrequencyBounds(final int tickFrequency) {
        return Math.max(tickFrequency, 1);
    }

    private static int enforceInitialDelayBounds(final int initialDelay) {
        return Math.max(initialDelay, 0);
    }

    public static TickStrategyFrequency always() {
        return new TickStrategyFrequency();
    }
}
