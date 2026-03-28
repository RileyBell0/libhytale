package dev.twunk.lib._archive.strategy;

public class TickStrategyFrequency extends TickStrategy {

    // i want to run every X ticks
    // - basically just means we want to SLEEP x seconds between each run for each
    // component, BUT, they don't necessarily need to be synchronised
    public final int tickFrequency;

    // When something starts ticking, this is how long we should DELAY until
    // its first tick gets run
    public final int initialDelay;

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

    private static final int enforceFrequencyBounds(final int tickFrequency) {
        return tickFrequency < 1 ? 1 : tickFrequency;
    }

    private static final int enforceInitialDelayBounds(final int initialDelay) {
        return initialDelay < 0 ? 0 : initialDelay;
    }

    public static final TickStrategyFrequency always() {
        return new TickStrategyFrequency();
    }
}
