package dev.twunk.hytale.interaction;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import dev.twunk.annotations.Serializable;
import dev.twunk.annotations.Serialize;
import dev.twunk.hytale.utils.ItemUtils;
import javax.annotation.Nonnull;

/**
 * @see com.hypixel.hytale.server.core.entity.ItemUtils I based most of my implementation on this
 * @see dev.twunk.hytale.utils.ItemUtils I extended hytales ItemUtils a bit, so my implementation uses this alot
 */
@Serializable(inherits = SimpleInstantInteraction.class)
public class SpawnItemInteraction extends SimpleInstantInteraction {

    @Nonnull
    @Serialize
    private Vector3i offset = new Vector3i(0, 0, 0);

    @Serialize
    private Vector3i at = null;

    @Nonnull
    @Serialize
    private String itemId = "Soil_Grass";

    @Serialize(min = 1)
    private int quantity = 1;

    @Override
    protected void firstRun(
        final @Nonnull InteractionType interactionType,
        final @Nonnull InteractionContext interactionContext,
        final @Nonnull CooldownHandler cooldownHandler
    ) {
        final var commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            return;
        }

        final var player = commandBuffer.getComponent(interactionContext.getOwningEntity(), Player.getComponentType());
        if (player == null) {
            return;
        }

        final var playerRef = player.getReference();
        if (playerRef == null) {
            return;
        }

        @Nonnull
        Vector3i coords;
        if (this.at != null) {
            coords = this.at;
        } else {
            final var targetBlockPos = interactionContext.getTargetBlock();
            if (targetBlockPos == null) {
                return;
            }

            coords = new Vector3i(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
        }

        coords = coords.add(this.offset);

        ItemUtils.spawn(playerRef, commandBuffer, coords, new ItemStack(this.itemId, this.quantity));
    }
}
