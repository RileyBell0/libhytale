package dev.twunk.interfaces.methods;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.component.ContainerComponent;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
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

    public default void onOpen(
        final @Nonnull Ref<EntityStore> ref,
        final @Nonnull World world,
        final @Nonnull Store<EntityStore> store
    ) {}

    // Hytale src code (deprecated) called this `isAllowViewing`
    public boolean canView();
    public boolean canOpen();
}
