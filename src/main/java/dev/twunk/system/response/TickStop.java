package dev.twunk.system.response;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.IRegisteredComponent;
import javax.annotation.Nonnull;

/**
 * Goodbye ticking forever
 */
public class TickStop implements TickResponse {

    @SuppressWarnings("null")
    @Nonnull
    public static ComponentType<ChunkStore, TickStop> COMPONENT_TYPE;

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<TickStop> CODEC = BuilderCodec.builder(TickStop.class, TickStop::new).build();

    @Nonnull
    public TickStop clone() {
        return new TickStop();
    }

    @SuppressWarnings("unused")
    @Nonnull
    public ComponentType<ChunkStore, ? extends TickStop> getComponentType() {
        if (COMPONENT_TYPE != null) {
            return COMPONENT_TYPE;
        }
        COMPONENT_TYPE = IRegisteredComponent.getComponentType(TickStop.class);
        return COMPONENT_TYPE;
    }

    @Override
    @Nonnull
    public String getType() {
        return TickResponse.TYPE_STOP;
    }
}
