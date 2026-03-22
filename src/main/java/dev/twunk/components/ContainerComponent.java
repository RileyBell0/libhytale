package dev.twunk.components;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class ContainerComponent implements IContainerComponent {

    @Nonnull
    @SuppressWarnings("null")
    public static ComponentType<ChunkStore, ContainerComponent> COMPONENT_TYPE;

    // private boolean canView = true;
    // private boolean canOpen = true;

    @Nonnull
    protected SimpleItemContainer container;

    @Nonnull
    public static final BuilderCodec<ContainerComponent> CODEC = BuilderCodec.builder(
        ContainerComponent.class,
        ContainerComponent::new
    )
        .append(
            new KeyedCodec<SimpleItemContainer>("Container", SimpleItemContainer.CODEC),
            (self, container) -> {
                if (container != null) {
                    self.container = container;
                }
            },
            self -> self.container
        )
        .add()
        .build();

    private static final short DEFAULT_CAPACITY = 10;

    @Nonnull
    private final Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<>();

    public ContainerComponent() {
        this.container = new SimpleItemContainer(DEFAULT_CAPACITY);
    }

    public ContainerComponent(final short capacity) {
        this.container = new SimpleItemContainer(capacity);
    }

    public ContainerComponent(@Nonnull final SimpleItemContainer container) {
        this.container = new SimpleItemContainer(container);
    }

    @Nonnull
    public Map<UUID, ContainerBlockWindow> getWindows() {
        return this.windows;
    }

    @Nonnull
    public SimpleItemContainer getContainer() {
        return this.container;
    }

    public short getCapacity() {
        return this.container.getCapacity();
    }

    public ContainerComponent clone() {
        return new ContainerComponent(this.container);
    }
}
