package dev.twunk.ticking.response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Don't tick until the given amount of time has passed (or until x ticks etc)
 *
 * notably, you can set this to be sleeping forever
 */
public class TickResponseSleep extends TickResponse {
    @Nullable
    public final Integer sleepForTicks;

    /**
     * Default: sleep forever
     */
    public TickResponseSleep() {
        this.sleepForTicks = null;
    }

    /**
     * Sleep for a given tick duration
     */
    public TickResponseSleep(int sleepForTicks) {
        this.sleepForTicks = sleepForTicks;
    }

    @Nonnull
    public static TickResponseSleep forSeconds(int seconds) {
        return new TickResponseSleep(30 * seconds);
    }

    @Nonnull
    public static TickResponseSleep forTicks(int ticks) {
        return new TickResponseSleep(ticks);
    }

    @Nonnull
    public static TickResponseSleep forEternity() {
        return new TickResponseSleep();
    }
}
