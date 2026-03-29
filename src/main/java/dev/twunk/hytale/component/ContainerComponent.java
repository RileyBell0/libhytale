package dev.twunk.hytale.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.annotations.Serialize;
import dev.twunk.hytale.utils.AutoCodecGenerator;
import dev.twunk.interfaces.component.IContainerComponent;
import dev.twunk.interfaces.methods.IContainer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * My code
 * @see IContainer - My interface for methods I need containers to fulfil to
 *                   show them in GUI
 *
 * Hytale's code
 * @see OpenContainerInteraction - Their interaction that opens containers
 */
public class ContainerComponent<ECS_TYPE> implements IContainerComponent<ECS_TYPE> {

    @Nonnull
    @SuppressWarnings({ "rawtypes" })
    private static final BuilderCodec<ContainerComponent> RAW_CODEC = AutoCodecGenerator.build(
        ContainerComponent.class,
        ContainerComponent::new
    );

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

    @Serialize
    private boolean canView = true;

    @Serialize
    private boolean canOpen = true;

    @Nonnull
    @Serialize
    protected SimpleItemContainer container;

    @Nullable
    public WorldChunk worldChunk = null;

    @Nullable
    private SimpleItemContainer registeredTo = null;

    @Nonnull
    private final Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<>();

    /////////////////////
    // Constructors
    /////////////////////

    public ContainerComponent() {
        this.container = new SimpleItemContainer(DEFAULT_CAPACITY);
        this.container.registerChangeEvent(EventPriority.LAST, this::onItemChange);
    }

    public ContainerComponent(final short capacity) {
        this.container = new SimpleItemContainer(capacity);
        this.container.registerChangeEvent(EventPriority.LAST, this::onItemChange);
    }

    public ContainerComponent(final @Nonnull SimpleItemContainer container) {
        this.container = new SimpleItemContainer(container);
        this.container.registerChangeEvent(EventPriority.LAST, this::onItemChange);
    }

    public void setChunk(@Nullable WorldChunk worldChunk) {
        this.worldChunk = worldChunk;
    }

    public void onItemChange(ItemContainer.ItemContainerChangeEvent event) {
        if (this.worldChunk == null) {
            return;
        }
        this.worldChunk.markNeedsSaving();
    }

    public void setContainer(SimpleItemContainer container) {
        container.registerChangeEvent(EventPriority.LAST, this::onItemChange);
        this.container = container;
    }

    @Nonnull
    public Map<UUID, ContainerBlockWindow> getWindows() {
        return this.windows;
    }

    @Nonnull
    public SimpleItemContainer getContainer() {
        if (this.registeredTo != this.container) {
            this.container.registerChangeEvent(EventPriority.LAST, this::onItemChange);
            this.registeredTo = this.container;
        }

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

    @Override
    @Nullable
    public WorldChunk getWorldChunk() {
        return this.worldChunk;
    }
}
