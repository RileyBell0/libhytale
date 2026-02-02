package dev.twunk.ticking.response;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.ITickingComponent;
import javax.annotation.Nonnull;

/**
 * Keep ticking at the same frequency as before
 */
public class TickContinue implements TickResponse {
    @Nonnull
    public static ComponentType<ChunkStore, TickContinue> COMPONENT_TYPE = ITickingComponent
            .getComponentType(TickContinue.class);

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<TickContinue> CODEC = BuilderCodec.builder(
            TickContinue.class,
            TickContinue::new)
            .build();

    @Nonnull
    public TickContinue clone() {
        return new TickContinue();
    }

    @Nonnull
    public ComponentType<ChunkStore, ? extends TickResponse> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register() {

    }
}
