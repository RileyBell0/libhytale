package dev.twunk.ticking.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class ModTickingSleepComponent implements Component<ChunkStore> {

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<ModTickingSleepComponent> CODEC = BuilderCodec.builder(
            ModTickingSleepComponent.class,
            ModTickingSleepComponent::new)
            .build();

    @Nonnull
    public ModTickingSleepComponent clone() {
        return new ModTickingSleepComponent();
    }

    @Nonnull
    public static ComponentType<ChunkStore, ModTickingSleepComponent> getComponentType() {
        return ITickingComponent.getComponentType(ModTickingSleepComponent.class);
    }
}
