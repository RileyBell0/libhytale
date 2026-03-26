package dev.twunk.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class ContainerComponent<ECS_TYPE> implements IContainerComponent<ECS_TYPE> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nonnull
    private static final BuilderCodec<ContainerComponent> RAW_CODEC = BuilderCodec.builder(
        ContainerComponent.class,
        ContainerComponent::new
    )
        // Container codec contains "Capacity" and "Items" fields
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
        .appendInherited(
            new KeyedCodec<>("CanView", Codec.BOOLEAN),
            (self, canView) -> {
                if (canView != null) {
                    self.canView = canView;
                }
            },
            self -> self.canView,
            (self, parent) -> self.canView = parent.canView
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("CanOpen", Codec.BOOLEAN),
            (self, canOpen) -> {
                if (canOpen != null) {
                    self.canOpen = canOpen;
                }
            },
            self -> self.canOpen,
            (self, parent) -> self.canOpen = parent.canOpen
        )
        .add()
        .build();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nonnull
    public static final BuilderCodec<ContainerComponent<EntityStore>> ENTITY_CODEC = (BuilderCodec<
        ContainerComponent<EntityStore>
    >) ((BuilderCodec) RAW_CODEC);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nonnull
    public static final BuilderCodec<ContainerComponent<ChunkStore>> CHUNK_CODEC = (BuilderCodec<
        ContainerComponent<ChunkStore>
    >) ((BuilderCodec) RAW_CODEC);

    private static final short DEFAULT_CAPACITY = 10;

    /////////////////////
    // INSTANCE VARIABLES
    /////////////////////

    private boolean canView = true;
    private boolean canOpen = true;

    @Nonnull
    protected SimpleItemContainer container;

    @Nonnull
    private final Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<>();

    /////////////////////
    // Constructors
    /////////////////////

    public ContainerComponent() {
        this.container = new SimpleItemContainer(DEFAULT_CAPACITY);
    }

    public ContainerComponent(final short capacity) {
        this.container = new SimpleItemContainer(capacity);
    }

    public ContainerComponent(final @Nonnull SimpleItemContainer container) {
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

    // IContainer::canView
    public boolean canView() {
        return this.canView;
    }

    // IContainer::canOpen
    public boolean canOpen() {
        return this.canOpen;
    }

    public ContainerComponent<ECS_TYPE> clone() {
        return new ContainerComponent<>(this.container);
    }
}
