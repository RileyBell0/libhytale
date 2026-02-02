package dev.twunk.ticking.response;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.ITickingComponent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Don't tick until the given amount of time has passed (or until x ticks etc)
 *
 * notably, you can set this to be sleeping forever
 */
public class TickSleep implements TickResponse {
    @SuppressWarnings("null")
    @Nonnull
    public static ComponentType<ChunkStore, TickSleep> COMPONENT_TYPE;
    @Nullable
    public final Integer sleepForTicks;

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<TickSleep> CODEC = BuilderCodec.builder(
            TickSleep.class,
            TickSleep::new)
            .build();

    @Nonnull
    public TickSleep clone() {
        return new TickSleep();
    }

    /**
     * Default: sleep forever
     */
    public TickSleep() {
        this.sleepForTicks = null;
    }

    /**
     * Sleep for a given tick duration
     */
    public TickSleep(int sleepForTicks) {
        this.sleepForTicks = sleepForTicks;
    }

    @Nonnull
    public static TickSleep forSeconds(int seconds) {
        return new TickSleep(30 * seconds);
    }

    @Nonnull
    public static TickSleep forTicks(int ticks) {
        return new TickSleep(ticks);
    }

    @Nonnull
    public static TickSleep forEternity() {
        return new TickSleep();
    }

    @SuppressWarnings("unused")
    @Nonnull
    public ComponentType<ChunkStore, ? extends TickResponse> getComponentType() {
        if (COMPONENT_TYPE != null) {
            return COMPONENT_TYPE;
        }
        COMPONENT_TYPE = ITickingComponent.getComponentType(TickSleep.class);
        return COMPONENT_TYPE;
    }
}
