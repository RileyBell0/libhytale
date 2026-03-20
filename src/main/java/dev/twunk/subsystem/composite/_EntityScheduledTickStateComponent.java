package dev.twunk.subsystem.composite;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.TwunkLib;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface _EntityScheduledTickStateComponent extends Component<ChunkStore> {
    public static final String TYPE_CONTINUE = "continue";
    public static final String TYPE_SLEEP = "sleep";
    public static final String TYPE_STOP = "stop";
    public static final String TYPE_BROKEN = "broken";

    @Nonnull
    public String getType();

    @Nonnull
    public static final _EntityScheduledTickStateComponent CONTINUE = (_EntityScheduledTickStateComponent) new Active();

    @Nonnull
    public static final _EntityScheduledTickStateComponent SLEEP = (_EntityScheduledTickStateComponent) new Sleeping();

    @Nonnull
    public static final _EntityScheduledTickStateComponent STOP = (_EntityScheduledTickStateComponent) new Stopped();

    @Nonnull
    public static final _EntityScheduledTickStateComponent BROKEN = (_EntityScheduledTickStateComponent) new Unknown();

    @Nonnull
    public abstract ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType();

    @Nonnull
    public abstract _EntityScheduledTickStateComponent clone();

    /**
     * Keep ticking at the same frequency as before
     */
    public class Active implements _EntityScheduledTickStateComponent {

        @SuppressWarnings("null")
        @Nonnull
        public static ComponentType<ChunkStore, Active> COMPONENT_TYPE;

        // serializing/deserializing your vars
        @Nonnull
        public static final BuilderCodec<Active> CODEC = BuilderCodec.builder(Active.class, Active::new).build();

        @Nonnull
        public Active clone() {
            return new Active();
        }

        @SuppressWarnings("unused")
        @Nonnull
        public ComponentType<ChunkStore, ? extends Active> getComponentType() {
            if (COMPONENT_TYPE != null) {
                return COMPONENT_TYPE;
            }
            COMPONENT_TYPE = TwunkLib.getComponentType(Active.class);
            return COMPONENT_TYPE;
        }

        @Override
        @Nonnull
        public String getType() {
            return _EntityScheduledTickStateComponent.TYPE_CONTINUE;
        }
    }

    /**
     * Don't tick until the given amount of time has passed (or until x ticks etc)
     *
     * notably, you can set this to be sleeping forever
     */
    public class Sleeping implements _EntityScheduledTickStateComponent {

        @SuppressWarnings("null")
        @Nonnull
        public static ComponentType<ChunkStore, Sleeping> COMPONENT_TYPE;

        @Nullable
        public final Integer sleepForTicks;

        // serializing/deserializing your vars
        @Nonnull
        public static final BuilderCodec<Sleeping> CODEC = BuilderCodec.builder(Sleeping.class, Sleeping::new).build();

        @Nonnull
        public Sleeping clone() {
            return new Sleeping();
        }

        /**
         * Default: sleep forever
         */
        public Sleeping() {
            this.sleepForTicks = null;
        }

        /**
         * Sleep for a given tick duration
         */
        public Sleeping(int sleepForTicks) {
            this.sleepForTicks = sleepForTicks;
        }

        public boolean isIndefinite() {
            return this.sleepForTicks == null;
        }

        @Nonnull
        public static Sleeping forSeconds(int seconds) {
            return new Sleeping(30 * seconds);
        }

        @Nonnull
        public static Sleeping forTicks(int ticks) {
            return new Sleeping(ticks);
        }

        @Nonnull
        public static Sleeping forEternity() {
            return new Sleeping();
        }

        @SuppressWarnings("unused")
        @Nonnull
        public ComponentType<ChunkStore, ? extends _EntityScheduledTickStateComponent> getComponentType() {
            if (COMPONENT_TYPE != null) {
                return COMPONENT_TYPE;
            }
            COMPONENT_TYPE = TwunkLib.getComponentType(Sleeping.class);
            return COMPONENT_TYPE;
        }

        @Override
        @Nonnull
        public String getType() {
            return _EntityScheduledTickStateComponent.TYPE_SLEEP;
        }
    }

    /**
     * Goodbye ticking forever
     */
    public class Stopped implements _EntityScheduledTickStateComponent {

        @SuppressWarnings("null")
        @Nonnull
        public static ComponentType<ChunkStore, Stopped> COMPONENT_TYPE;

        // serializing/deserializing your vars
        @Nonnull
        public static final BuilderCodec<Stopped> CODEC = BuilderCodec.builder(Stopped.class, Stopped::new).build();

        @Nonnull
        public Stopped clone() {
            return new Stopped();
        }

        @SuppressWarnings("unused")
        @Nonnull
        public ComponentType<ChunkStore, ? extends Stopped> getComponentType() {
            if (COMPONENT_TYPE != null) {
                return COMPONENT_TYPE;
            }
            COMPONENT_TYPE = TwunkLib.getComponentType(Stopped.class);
            return COMPONENT_TYPE;
        }

        @Override
        @Nonnull
        public String getType() {
            return _EntityScheduledTickStateComponent.TYPE_STOP;
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
    public class Unknown implements _EntityScheduledTickStateComponent {

        @SuppressWarnings("null")
        @Nonnull
        public static ComponentType<ChunkStore, Unknown> COMPONENT_TYPE;

        // serializing/deserializing your vars
        @Nonnull
        public static final BuilderCodec<Unknown> CODEC = BuilderCodec.builder(Unknown.class, Unknown::new).build();

        @Nonnull
        public Unknown clone() {
            return new Unknown();
        }

        @SuppressWarnings("unused")
        @Nonnull
        public ComponentType<ChunkStore, ? extends Unknown> getComponentType() {
            if (COMPONENT_TYPE != null) {
                return COMPONENT_TYPE;
            }
            COMPONENT_TYPE = TwunkLib.getComponentType(Unknown.class);
            return COMPONENT_TYPE;
        }

        @Override
        @Nonnull
        public String getType() {
            return _EntityScheduledTickStateComponent.TYPE_BROKEN;
        }
    }
}
