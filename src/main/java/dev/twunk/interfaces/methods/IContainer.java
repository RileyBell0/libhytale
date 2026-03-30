package dev.twunk.interfaces.methods;

import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.component.ContainerComponent;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
import dev.twunk.hytale.refs.AnyRef;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

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
    @Nonnull
    public Map<UUID, ContainerBlockWindow> getWindows();

    @Nonnull
    public SimpleItemContainer getContainer();

    public short getCapacity();

    public default <ECS_STORE extends WorldProvider> void onOpen(final @Nonnull AnyRef<ECS_STORE> ref) {}

    // Hytale src code (deprecated) called this `isAllowViewing`
    public default boolean canView() {
        return true;
    }

    public default boolean canOpen() {
        return true;
    }
}
