package dev.twunk.hytale.interfaces.methods;

import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.component.ContainerComponent;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
import dev.twunk.hytale.refs.AnyRef;
import java.util.Map;
import java.util.UUID;

/**
 * My code
 * @see ContainerComponent                - Basically just this + Component
 * @see OpenContainerComponentInteraction - My interaction that'll open a GUI for the ContainerComponent
 *                                          on the block it's attached to
 *
 * Hytale's code
 * @see OpenContainerInteraction - Their interaction that opens containers
 */
public interface IContainer {
    public Map<UUID, ContainerBlockWindow> getWindows();

    public SimpleItemContainer getContainer();

    public short getCapacity();

    public default <ECS_TYPE extends WorldProvider> void onOpen(final AnyRef<ECS_TYPE> ref) {}

    // Hytale src code (deprecated) called this `isAllowViewing`
    public default boolean canView() {
        return true;
    }

    public default boolean canOpen() {
        return true;
    }
}
