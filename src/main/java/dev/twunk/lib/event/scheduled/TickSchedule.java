package dev.twunk.lib.event.scheduled;

public abstract sealed interface TickSchedule permits TickSchedule.Active, TickSchedule.Sleeping, TickSchedule.Stopped {
    public static final TickSchedule ACTIVE = new Active();
    public static final TickSchedule STOP = new Stopped();

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Keep ticking at the same frequency as before
     */
    public final class Active implements TickSchedule {}

    /**
     * Don't tick until the given amount of time has passed (or until x ticks etc)
     *
     * notably, you can set this to be sleeping forever
     */
    public final class Sleeping implements TickSchedule {

        public final int wakeUpAt;

        /**
         * Default: sleep forever
         */
        public Sleeping(int wakeUpTick) {
            this.wakeUpAt = wakeUpTick;
        }

        public static Sleeping forSeconds(final int seconds) {
            return new Sleeping(30 * seconds);
        }

        public static Sleeping forTicks(final int ticks) {
            return new Sleeping(ticks);
        }
    }

    /**
     * Goodbye ticking forever
     */
    public final class Stopped implements TickSchedule {}
}
