package dev.twunk.hytale.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
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
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.TwunkLib;
import dev.twunk.hytale.components.ContainerComponent;
import dev.twunk.hytale.components.TrashComponent;
import dev.twunk.hytale.interfaces.IContainer;
import dev.twunk.hytale.interfaces.component.IContainerComponent;
import dev.twunk.hytale.utils.ContainerUtils;
import javax.annotation.Nonnull;
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
 * @see ItemContainerState       - The "BlockState" (deprecated) that seems to
 *                                 store container information
 * @see OpenContainerInteraction - Their interaction that opens containers
 */
public class OpenContainerComponentInteraction extends SimpleBlockInteraction {

    private static final @Nonnull String DEFAULT_COMPONENT_ID = "dev.twunk.hytale.components.ContainerComponent";

    @SuppressWarnings("unchecked")
    public static final @Nonnull BuilderCodec<OpenContainerComponentInteraction> CODEC = BuilderCodec.builder(
        OpenContainerComponentInteraction.class,
        OpenContainerComponentInteraction::new,
        SimpleBlockInteraction.CODEC
    )
        .documentation(
            "Opens the container of the block currently being interacted with based on its ContainerComponent."
        )
        .append(
            new KeyedCodec<String>("ComponentId", Codec.STRING),
            (self, id) -> {
                if (id == null) {
                    return;
                }
                final var potentialComponentType = TwunkLib.getChunkComponentType(id);
                if (potentialComponentType == null) {
                    // I'm deciding to crash gracefully here
                    return;
                }

                final var innerClass = potentialComponentType.getTypeClass();
                if (!IContainerComponent.class.isAssignableFrom(innerClass)) {
                    return;
                }

                self.componentType = (ComponentType<
                    ChunkStore,
                    ? extends IContainerComponent<ChunkStore>
                >) potentialComponentType;
                self.componentId = id;
            },
            self -> self.componentId
        )
        .add()
        .build();

    /////////////////////
    // INSTANCE VARIABLES
    /////////////////////

    @SuppressWarnings("unchecked")
    private @Nonnull ComponentType<ChunkStore, ? extends IContainerComponent<ChunkStore>> componentType =
        TwunkLib.getChunkComponentType(ContainerComponent.class);

    /**
     * The ID that identifies the specific container component to look for
     *
     * we have to do a sort of backwards join (i forget the name) from
     * - Interactions are placed on block definitions (on disk, in json)
     * - When we RUN an interaction for a block, we don't know what component
     *   find & open (i don't want to define an interaction for each one), SO
     *   we'll just have the caller write to disk the ID of the component
     *   their `OpenContainer` interaction is targeting
     *
     * in the future, i plan to make some sort of registry when registering
     * component types to keep track of inventory components, that way an interaction
     * can show ALL inventories attached to a block/entity, not JUST a specific
     * one (though that should likely be its own interaction OR a config option
     * for this interaction)
     */

    private @Nonnull String componentId = DEFAULT_COMPONENT_ID;

    /////////////////////
    // Constructors (lmao)
    /////////////////////

    public OpenContainerComponentInteraction() {}

    @Override
    protected void interactWithBlock(
        final @Nonnull World world2,
        final @Nonnull CommandBuffer<EntityStore> commandBuffer,
        final @Nonnull InteractionType type,
        final @Nonnull InteractionContext context,
        final @Nullable ItemStack itemInHand,
        final @Nonnull Vector3i pos,
        final @Nonnull CooldownHandler cooldownHandler
    ) {
        ContainerUtils.openContainerGUI(this.componentType, commandBuffer, context, pos);
    }

    @Override
    protected void simulateInteractWithBlock(
        final @Nonnull InteractionType type,
        final @Nonnull InteractionContext context,
        final @Nullable ItemStack itemInHand,
        final @Nonnull World world,
        final @Nonnull Vector3i targetBlock
    ) {}
}
