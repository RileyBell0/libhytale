package dev.twunk.ticking.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class ModTickingAwakeComponent implements Component<ChunkStore> {

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<ModTickingAwakeComponent> CODEC = BuilderCodec.builder(
            ModTickingAwakeComponent.class,
            ModTickingAwakeComponent::new)
            .build();

    @Nonnull
    public ModTickingAwakeComponent clone() {
        return new ModTickingAwakeComponent();
    }

    @Nonnull
    public static ComponentType<ChunkStore, ModTickingAwakeComponent> getComponentType() {
        return ITickingComponent.getComponentType(ModTickingAwakeComponent.class);
    }
}
