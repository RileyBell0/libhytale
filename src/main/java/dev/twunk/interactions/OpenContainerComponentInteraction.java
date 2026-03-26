package dev.twunk.interactions;

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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.TwunkLib;
import dev.twunk.components.ContainerComponent;
import dev.twunk.components.IContainerComponent;
import dev.twunk.utils.ContainerUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenContainerComponentInteraction extends SimpleBlockInteraction {

    @Nonnull
    private static final String DEFAULT_COMPONENT_ID = "dev.twunk.components.ContainerComponent";

    @SuppressWarnings("unchecked")
    @Nonnull
    private ComponentType<ChunkStore, ? extends IContainerComponent<ChunkStore>> componentType =
        TwunkLib.getChunkComponentType(ContainerComponent.class);

    @Nonnull
    private String componentId = DEFAULT_COMPONENT_ID;

    @SuppressWarnings("unchecked")
    @Nonnull
    public static final BuilderCodec<OpenContainerComponentInteraction> CODEC = BuilderCodec.builder(
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
