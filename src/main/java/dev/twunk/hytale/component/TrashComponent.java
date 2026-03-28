package dev.twunk.hytale.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
import dev.twunk.interfaces.component.IContainerComponent;
import dev.twunk.interfaces.methods.IContainer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO add a "timeout" for trash inventories, so when you CLOSE the inventory i go "ok yeah i get you, you
 * want to delete these items. Just gonna make SURE you're sure, by keeping them around for like, 30 seconds"
 *
 * limit it to only be the player that opened it that can see those inventories.
 * make it QUEUE all inventories for stuff that was deleted. if you open the trash it should pause all inventory deletions
 * and resume the countdown when you close the trash
 *
 * My code
 * @see OpenContainerComponentInteraction - the interaction i wrote that is capable of
 *                                          opening a GUI for the block a trash can is on.
 * @see ContainerComponent                - A regular 'ol container without trash can schenanigans
 * @see IContainer                        - My interface for methods I need containers to
 *                                          fulfil to show them in GUI
 *
 * Hytale's code
 * @see OpenContainerInteraction - Their interaction that opens containers
 */
public class TrashComponent<ECS_STORE extends WorldProvider> implements IContainerComponent<ECS_STORE> {

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

    /////////////////////
    // INSTANCE VARIABLES
    /////////////////////

    private short capacity = DEFAULT_CAPACITY;

    private boolean canView = true;
    private boolean canOpen = true;

    @Nonnull
    private final Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<>();

    /////////////////////
    // Constructors
    /////////////////////

    public TrashComponent() {
        this.capacity = DEFAULT_CAPACITY;
    }

    public TrashComponent(final short capacity) {
        this.capacity = capacity;
    }

    public TrashComponent(final @Nonnull SimpleItemContainer container) {
        this.capacity = container.getCapacity();
    }

    @Override
    @Nonnull
    public Map<UUID, ContainerBlockWindow> getWindows() {
        return this.windows;
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

    // IContainer::canView
    public boolean canView() {
        return this.canView;
    }

    // IContainer::canOpen
    public boolean canOpen() {
        return this.canOpen;
    }

    @Override
    public TrashComponent<ECS_STORE> clone() {
        return new TrashComponent<ECS_STORE>(this.capacity);
    }

    @Override
    public void setChunk(@Nullable WorldChunk worldChunk) {}

    @Override
    @Nullable
    public WorldChunk getWorldChunk() {
        return null;
    }
}
