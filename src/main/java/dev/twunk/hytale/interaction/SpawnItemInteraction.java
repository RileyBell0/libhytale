package dev.twunk.hytale.interaction;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import dev.twunk.annotations.Serializable;
import dev.twunk.annotations.Serialize;
import dev.twunk.hytale.utils.ItemUtils;
import javax.annotation.Nullable;

/**
 * @see com.hypixel.hytale.server.core.entity.ItemUtils I based most of my implementation on this
 * @see dev.twunk.hytale.utils.ItemUtils I extended hytales ItemUtils a bit, so my implementation uses this alot
 */
@Serializable(
    inherits = SimpleInstantInteraction.class,
    documentation = "Interaction that simply spawns an item when run at the location specified (defaults to position of target entity"
)
public class SpawnItemInteraction extends SimpleInstantInteraction {

    /** X, Y, Z Offset from the interacted block */
    @Serialize
    private Vector3d offset = new Vector3d(0, 0, 0);

    /**
     * Position to spawn item. If value is null, will spawn at the location
     * of the interacted block + offset
     */
    @Serialize
    @Nullable
    private Vector3d at = null;

    /** ID of the item to spawn */
    @Serialize
    private String itemId = "Soil_Grass";

    /** Quantity of items (within the item stack) to spawns */
    @Serialize(min = 1)
    private int quantity = 1;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /** Function that's run when the interaction is activated */
    @Override
    protected void firstRun(
        final InteractionType interactionType,
        final InteractionContext interactionContext,
        final CooldownHandler cooldownHandler
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

        Vector3d coords;
        if (this.at != null) {
            coords = this.at;
        } else {
            final var targetBlockPos = interactionContext.getTargetBlock();
            if (targetBlockPos == null) {
                return;
            }

            coords = new Vector3d(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
        }

        coords = coords.add(this.offset);

        ItemUtils.spawn(playerRef, commandBuffer, coords, new ItemStack(this.itemId, this.quantity));
    }
}
