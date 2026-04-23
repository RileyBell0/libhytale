package dev.twunk.hytale.component;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.hytale.interfaces.component.IContainerComponent;
import dev.twunk.hytale.interfaces.methods.IContainer;
import dev.twunk.hytale.interfaces.methods.IPersistentContainer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
public class ContainerComponent<
    ECS_TYPE extends WorldProvider
> implements IContainerComponent<ECS_TYPE>, IPersistentContainer {

    //--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/-
    //==> Codec

    @Serialize
    private boolean canView = true;

    @Serialize
    private boolean canOpen = true;

    @Serialize
    protected SimpleItemContainer container = new SimpleItemContainer((short) 10);

    //--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/-

    @Nullable
    public WorldChunk worldChunk = null;

    @Nullable
    private SimpleItemContainer registeredTo = null;

    private final Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    // IContainer::getWindows
    @Override
    public Map<UUID, ContainerBlockWindow> getWindows() {
        return this.windows;
    }

    // IContainer::getContainer
    @Override
    public SimpleItemContainer getContainer() {
        if (this.registeredTo != this.container) {
            this.container.registerChangeEvent(EventPriority.LAST, this::onItemChange);
            this.registeredTo = this.container;
        }

        return this.container;
    }

    // IPersistentContainer::onItemChange
    @Override
    public void onItemChange(ItemContainer.ItemContainerChangeEvent event) {
        if (this.worldChunk == null) {
            return;
        }

        this.worldChunk.markNeedsSaving();
    }

    // IPersistentContainer::setChunk
    @Override
    public void setChunk(@Nullable WorldChunk worldChunk) {
        this.worldChunk = worldChunk;
    }

    // IContainer::getCapacity
    @Override
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

    // IPersistentContainer::getWorldChunk
    @Override
    @Nullable
    public WorldChunk getWorldChunk() {
        return this.worldChunk;
    }

    public ContainerComponent<ECS_TYPE> clone() {
        var component = new ContainerComponent<ECS_TYPE>();
        component.container = this.container.clone();
        component.canView = this.canView;
        component.canOpen = this.canOpen;

        return component;
    }
}
