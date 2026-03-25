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

// TODO add a "timeout" for inventories, so when you CLOSE the inventory i go "ok yeah i get you, you
// want to delete these items. Just gonna make SURE you're sure, by keeping them around for like, 30 seconds"
//
// and i wanna do that with ALL non-empty inventories that are put on a trash componnet
// AND limit it to only be the player that opened it that can see those inventories
public class TrashComponent<ECS_TYPE> implements IContainerComponent<ECS_TYPE> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nonnull
    private static final BuilderCodec<TrashComponent> RAW_CODEC = BuilderCodec.builder(
        TrashComponent.class,
        TrashComponent::new
    )
        .appendInherited(
            new KeyedCodec<>("Capacity", Codec.SHORT),
            (self, capacity) -> {
                if (capacity != null) {
                    self.capacity = capacity;
                }
            },
            self -> self.capacity,
            (self, parent) -> self.capacity = parent.capacity
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

    private static final short DEFAULT_CAPACITY = 45;

    @Nonnull
    private final Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<>();

    private short capacity = DEFAULT_CAPACITY;

    // TODO write up canView and canOpen logic
    private boolean canView = true;
    private boolean canOpen = true;

    public TrashComponent() {
        this.capacity = DEFAULT_CAPACITY;
    }

    public TrashComponent(final short capacity) {
        this.capacity = capacity;
    }

    public TrashComponent(final @Nonnull SimpleItemContainer container) {
        this.capacity = container.getCapacity();
    }

    @Nonnull
    @Override
    public SimpleItemContainer getContainer() {
        return new SimpleItemContainer(this.capacity);
    }

    @Override
    public short getCapacity() {
        return this.capacity;
    }

    @Override
    public TrashComponent<ECS_TYPE> clone() {
        return new TrashComponent<ECS_TYPE>(this.capacity);
    }

    @Override
    @Nonnull
    public Map<UUID, ContainerBlockWindow> getWindows() {
        return this.windows;
    }
}
