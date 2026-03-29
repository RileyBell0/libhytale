package dev.twunk.interfaces.methods;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import dev.twunk.hytale.component.ContainerComponent;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
import javax.annotation.Nullable;

/**
 * My code
 * @see ContainerComponent                - Basically just this + Component
 * @see OpenContainerComponentInteraction - My interaction that'll open a GUI for the ContainerComponent
 *                                          on the block it's attached to
 *
 * Hytale's code
 * @see OpenContainerInteraction - Their interaction that opens containers
 */
public interface IPersistentContainer extends IContainer {
    // for saving
    public void setChunk(@Nullable WorldChunk worldChunk);

    @Nullable
    public WorldChunk getWorldChunk();

    // for, well, saving
    public default void onItemChange(ItemContainer.ItemContainerChangeEvent event) {
        var worldChunk = this.getWorldChunk();
        if (worldChunk == null) {
            return;
        }

        worldChunk.markNeedsSaving();
    }
}
