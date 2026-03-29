package dev.twunk.hytale.component;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import dev.twunk.annotations.Serializable;
import dev.twunk.annotations.Serialize;
import dev.twunk.interfaces.component.IContainerComponent;
import dev.twunk.interfaces.methods.IContainer;
import dev.twunk.interfaces.methods.IPersistentContainer;
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
@Serializable
public class ContainerComponent<ECS_TYPE> implements IContainerComponent<ECS_TYPE>, IPersistentContainer {

    private static final short DEFAULT_CAPACITY = 10;

    /////////////////////
    // INSTANCE VARIABLES
    /////////////////////

    @Serialize
    private boolean canView = true;

    @Serialize
    private boolean canOpen = true;

    @Serialize
    @Nonnull
    protected SimpleItemContainer container;

    // not-saved \/ \/

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
    }

    public ContainerComponent(final @Nonnull SimpleItemContainer container) {
        this.container = new SimpleItemContainer(container);
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
    @Override
    public boolean canView() {
        return this.canView;
    }

    // IContainer::canOpen
    @Override
    public boolean canOpen() {
        return this.canOpen;
    }

    @Override
    @Nullable
    public WorldChunk getWorldChunk() {
        return this.worldChunk;
    }

    public ContainerComponent<ECS_TYPE> clone() {
        return new ContainerComponent<>(this.container);
    }
}
