package dev.twunk.ticking.strategy;

public class TickStrategyFrequencySynced extends TickStrategy {
    // i want to run every X ticks
    // - Importantly, all entities of the given type are synced for this
    // meaning, they will still be "sleeping" but by predetermined amounts
    public final int tickFrequency;

    // how many ticks we should delay this by, e.g. if you want to tick every 30
    // ticks since the server started AND you want it to be specifically on the 15th
    // tick (repeating) then you should set delay to 15
    public final int tickOffset;

    // When something starts ticking, this is how long we should DELAY until
    // its first tick gets run
    //
    // In this case since we're synced, this means it will run on the first synced
    // AFTER `initialDelay` ticks have passed
    public final int initialDelay;

    public TickStrategyFrequencySynced() {
        // default: from the first tick onwards
        this.tickOffset = 0;

        // tick every single tick
        this.tickFrequency = 1;

        this.initialDelay = 0;
    }

    public TickStrategyFrequencySynced(int tickFrequency) {
        this.tickFrequency = tickFrequency;
        this.initialDelay = 0;
        this.tickOffset = 0;
    }

    public TickStrategyFrequencySynced(int tickFrequency, int initialDelay) {
        this.tickFrequency = tickFrequency;
        this.initialDelay = initialDelay;
        this.tickOffset = 0;
    }

    public TickStrategyFrequencySynced(int tickFrequency, int initialDelay, int tickOffset) {
        this.tickFrequency = tickFrequency;
        this.initialDelay = initialDelay;
        this.tickOffset = tickOffset;
    }

}