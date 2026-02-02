package dev.twunk.ticking.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class ModTickingIgnoredComponent implements Component<ChunkStore> {

    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<ModTickingIgnoredComponent> CODEC = BuilderCodec.builder(
            ModTickingIgnoredComponent.class,
            ModTickingIgnoredComponent::new)
            .build();

    @Nonnull
    public ModTickingIgnoredComponent clone() {
        return new ModTickingIgnoredComponent();
    }

    @Nonnull
    public static ComponentType<ChunkStore, ModTickingIgnoredComponent> getComponentType() {
        return ITickingComponent.getComponentType(ModTickingIgnoredComponent.class);
    }
}
