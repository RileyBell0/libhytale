package dev.twunk.hytale.interaction;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.codec.annotations.Serializable;
import dev.twunk.hytale.codec.annotations.Serialize;
import dev.twunk.hytale.component.ContainerComponent;
import dev.twunk.hytale.component.TrashComponent;
import dev.twunk.hytale.utils.ContainerUtils;
import dev.twunk.interfaces.component.IContainerComponent;
import dev.twunk.interfaces.methods.IContainer;
import javax.annotation.Nullable;

/**
 * @see ContainerComponent                - a basic container. Effectively, a block + ContainerComponent
 *                                          + OpenContainerComponentInteraction is effectively just a chest
 * @see IContainerComponent               - IContainer + Component -> simplest form of a container in a component
 * @see IContainer                        - The minimum requirements something needs to satisfy to be considered a container
 * @see TrashComponent                    - an alternative implementation of IContainerComponent, showing you
 *                                          other ways of utilising containers for more unique use-cases
 * @see OpenContainerComponentInteraction - my interaction for opening a GUI for containers stored in a component
 *                                          on a block (specifically extensions or implementors of IContainerComponent)
 *
 * Hytale's code that I based my implementation around
 * @see OpenContainerInteraction - Their interaction that opens containers
 */
@Serializable(
    inherits = SimpleBlockInteraction.class,
    documentation = "Opens the container of the block currently being interacted with based on its ContainerComponent."
)
public class OpenContainerComponentInteraction extends SimpleBlockInteraction {

    //--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/-
    //==> Codec

    /**
     * Pass in the ID of the container component that this interaction will search for
     * and open on the interacted entity
     */
    @SuppressWarnings({ "unchecked", "null" })
    @Serialize
    private ComponentType<ChunkStore, ? extends IContainerComponent<ChunkStore>> componentType =
        LibHytale.getChunkComponentType(ContainerComponent.class);

    //--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/--/-

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void interactWithBlock(
        final World world2,
        final CommandBuffer<EntityStore> commandBuffer,
        final InteractionType type,
        final InteractionContext context,
        final @Nullable ItemStack itemInHand,
        final Vector3i pos,
        final CooldownHandler cooldownHandler
    ) {
        ContainerUtils.openContainerGUI(this.componentType, commandBuffer, context, pos);
    }

    @Override
    protected void simulateInteractWithBlock(
        final InteractionType type,
        final InteractionContext context,
        final @Nullable ItemStack itemInHand,
        final World world,
        final Vector3i targetBlock
    ) {}
}
