package dev.twunk.ticking.response;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.IRegisteredComponent;
import javax.annotation.Nonnull;

/**
 * Keep ticking at the same frequency as before
 */
public class TickContinue implements TickResponse {

    @SuppressWarnings("null")
    @Nonnull
    public static ComponentType<ChunkStore, TickContinue> COMPONENT_TYPE;

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<TickContinue> CODEC = BuilderCodec.builder(
        TickContinue.class,
        TickContinue::new
    ).build();

    @Nonnull
    public TickContinue clone() {
        return new TickContinue();
    }

    @SuppressWarnings("unused")
    @Nonnull
    public ComponentType<ChunkStore, ? extends TickContinue> getComponentType() {
        if (COMPONENT_TYPE != null) {
            return COMPONENT_TYPE;
        }
        COMPONENT_TYPE = IRegisteredComponent.getComponentType(TickContinue.class);
        return COMPONENT_TYPE;
    }

    @Override
    @Nonnull
    public String getType() {
        return TickResponse.TYPE_CONTINUE;
    }
}
