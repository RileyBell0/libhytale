package dev.twunk.ticking.response;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.IRegisteredComponent;
import javax.annotation.Nonnull;

/**
 * A way to mark that we didn't stop because we wanted to, we stoped because we
 * failed
 * 
 * Good for if you depend on alot of external stuff, and if that external stuff
 * is mising one day, this is a way to *politely* shut down and NOT run every
 * single tick (when you know its broken) BUT still lets you, the developer,
 * have the OPTION of re-trying every now and then if you so desire
 */
public class TickBroken implements TickResponse {
    @SuppressWarnings("null")
    @Nonnull
    public static ComponentType<ChunkStore, TickBroken> COMPONENT_TYPE;

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<TickBroken> CODEC = BuilderCodec.builder(
            TickBroken.class,
            TickBroken::new)
            .build();

    @Nonnull
    public TickBroken clone() {
        return new TickBroken();
    }

    @SuppressWarnings("unused")
    @Nonnull
    public ComponentType<ChunkStore, ? extends TickBroken> getComponentType() {
        if (COMPONENT_TYPE != null) {
            return COMPONENT_TYPE;
        }
        COMPONENT_TYPE = IRegisteredComponent.getComponentType(TickBroken.class);
        return COMPONENT_TYPE;
    }
}
