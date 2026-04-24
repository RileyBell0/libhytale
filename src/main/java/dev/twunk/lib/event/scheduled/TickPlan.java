package dev.twunk.lib.event.scheduled;

import javax.annotation.Nullable;

public sealed interface TickPlan permits TickPlan.Active, TickPlan.Sleeping, TickPlan.Stopped, TickPlan.Unknown {
    public ScheduleType getType();

    public static final TickPlan CONTINUE = new Active();
    public static final TickPlan SLEEP = new Sleeping();
    public static final TickPlan STOP = new Stopped();
    public static final TickPlan BROKEN = new Unknown();

    public enum ScheduleType {
        ACTIVE,
        SLEEP,
        STOP,
        UNKNOWN
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Keep ticking at the same frequency as before
     */
    public final class Active implements TickPlan {

        @Override
        public ScheduleType getType() {
            return ScheduleType.ACTIVE;
        }
    }

    /**
     * Don't tick until the given amount of time has passed (or until x ticks etc)
     *
     * notably, you can set this to be sleeping forever
     */
    public final class Sleeping implements TickPlan {

        @Nullable
        public final Integer sleepForTicks;

        /**
         * Default: sleep forever
         */
        public Sleeping() {
            this.sleepForTicks = null;
        }

        /**
         * Sleep for a given tick duration
         */
        public Sleeping(final int sleepForTicks) {
            this.sleepForTicks = sleepForTicks;
        }

        public boolean isIndefinite() {
            return this.sleepForTicks == null;
        }

        public static Sleeping forSeconds(final int seconds) {
            return new Sleeping(30 * seconds);
        }

        public static Sleeping forTicks(final int ticks) {
            return new Sleeping(ticks);
        }

        public static Sleeping forEternity() {
            return new Sleeping();
        }

        @Override
        public ScheduleType getType() {
            return ScheduleType.SLEEP;
        }
    }

    /**
     * Goodbye ticking forever
     */
    public final class Stopped implements TickPlan {

        @Override
        public ScheduleType getType() {
            return ScheduleType.STOP;
        }
    }

    /**
     * A way to mark that we didn't stop because we wanted to, we stoped because we
     * failed
     *
     * Good for if you depend on alot of external stuff, and if that external stuff
     * is mising one day, this is a way to *politely* shut down and NOT run every
     * single tick (when you know its broken) BUT still lets you, the developer,
     * have the OPTION of re-trying every now and then if you so desire
     */
    public final class Unknown implements TickPlan {

        @Override
        public ScheduleType getType() {
            return ScheduleType.UNKNOWN;
        }
    }
}
