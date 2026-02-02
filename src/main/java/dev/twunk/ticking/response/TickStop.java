package dev.twunk.ticking.response;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.ITickingComponent;
import javax.annotation.Nonnull;

/**
 * Goodbye ticking forever
 */
public class TickStop implements TickResponse {
    @Nonnull
    public static ComponentType<ChunkStore, ? extends TickStop> COMPONENT_TYPE = ITickingComponent
            .getComponentType(TickStop.class);

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<TickStop> CODEC = BuilderCodec.builder(
            TickStop.class,
            TickStop::new)
            .build();

    @Nonnull
    public TickStop clone() {
        return new TickStop();
    }

    @Nonnull
    public ComponentType<ChunkStore, ? extends TickResponse> getComponentType() {
        return COMPONENT_TYPE;
    }

}
