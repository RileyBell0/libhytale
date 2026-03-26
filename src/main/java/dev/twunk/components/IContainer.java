package dev.twunk.components;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

public interface IContainer {
    @Nonnull
    public Map<UUID, ContainerBlockWindow> getWindows();

    @Nonnull
    public SimpleItemContainer getContainer();

    public short getCapacity();

    public default void onOpen(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull World world,
        @Nonnull Store<EntityStore> store
    ) {}

    // Hytale src code (deprecated) called this `isAllowViewing`
    public boolean canView();
    public boolean canOpen();
}
