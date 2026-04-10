package dev.twunk.lib;

import javax.annotation.Nullable;

public interface TickPlan {
    public static final String TYPE_CONTINUE = "continue";
    public static final String TYPE_SLEEP = "sleep";
    public static final String TYPE_STOP = "stop";
    public static final String TYPE_BROKEN = "broken";

    public String getType();

    public static final TickPlan CONTINUE = (TickPlan) new Active();

    public static final TickPlan SLEEP = (TickPlan) new Sleeping();

    public static final TickPlan STOP = (TickPlan) new Stopped();

    public static final TickPlan BROKEN = (TickPlan) new Unknown();

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Keep ticking at the same frequency as before
     */
    public class Active implements TickPlan {

        @Override
        public String getType() {
            return TickPlan.TYPE_CONTINUE;
        }
    }

    /**
     * Don't tick until the given amount of time has passed (or until x ticks etc)
     *
     * notably, you can set this to be sleeping forever
     */
    public class Sleeping implements TickPlan {

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
        public String getType() {
            return TickPlan.TYPE_SLEEP;
        }
    }

    /**
     * Goodbye ticking forever
     */
    public class Stopped implements TickPlan {

        @Override
        public String getType() {
            return TickPlan.TYPE_STOP;
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
    public class Unknown implements TickPlan {

        @Override
        public String getType() {
            return TickPlan.TYPE_BROKEN;
        }
    }
}
