package dev.twunk.hytale.component;

import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.codec.annotations.Serializable;
import dev.twunk.hytale.codec.annotations.Serialize;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
import dev.twunk.interfaces.component.IContainerComponent;
import dev.twunk.interfaces.methods.IContainer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
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
@Serializable
public class TrashComponent<ECS_TYPE extends WorldProvider> implements IContainerComponent<ECS_TYPE> {

    //--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/-
    //==> Codec

    @Serialize
    private short capacity = 45;

    @Serialize
    private boolean canView = true;

    @Serialize
    private boolean canOpen = true;

    //--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/-

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
        return new SimpleItemContainer(this.capacity);
    }

    // IContainer::canView
    @Override
    public short getCapacity() {
        return this.capacity;
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
    public TrashComponent<ECS_TYPE> clone() {
        var trash = new TrashComponent<ECS_TYPE>();
        trash.capacity = this.capacity;
        trash.canOpen = this.canOpen;
        trash.canView = this.canView;

        return trash;
    }
}
