package dev.twunk.hytale.interfaces.methods;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.component.ContainerComponent;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
import java.util.Map;
import java.util.UUID;

/// My code
/// @see ContainerComponent                - Basically just this + Component
/// @see OpenContainerComponentInteraction - My interaction that'll open a GUI for the ContainerComponent
///                                          on the block it's attached to
///
/// Hytale's code
/// @see OpenContainerInteraction - Their interaction that opens containers
public interface IContainer {
     Map<UUID, ContainerBlockWindow> getWindows();

     SimpleItemContainer getContainer();

     short getCapacity();

     default <ECS_TYPE extends WorldProvider> void onOpen(final Ref<ECS_TYPE> ref) {
        // TODO not sure if this is used
    }

    // Hytale src code (deprecated) called this `isAllowViewing`
     default boolean canView() {
        return true;
    }

     default boolean canOpen() {
        return true;
    }
}
