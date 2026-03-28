package dev.twunk.hytale.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry.Assets;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.hytale.utils.ItemUtils;
import javax.annotation.Nonnull;

/**
 * @see com.hypixel.hytale.server.core.entity.ItemUtils I based most of my implementation on this
 * @see dev.twunk.hytale.utils.ItemUtils I extended hytales ItemUtils a bit, so my implementation uses this alot
 */
public class SpawnItemInteraction extends SimpleInstantInteraction {

    @Nonnull
    private String direction = "none";

    @Nonnull
    private Vector3i offset = new Vector3i(0, 0, 0);

    private Vector3i at = null;

    @Nonnull
    private String itemId = "Soil_Grass";

    private int quantity = 1;

    @Nonnull
    public static final BuilderCodec<SpawnItemInteraction> CODEC = BuilderCodec.builder(
        SpawnItemInteraction.class,
        SpawnItemInteraction::new,
        SimpleInstantInteraction.CODEC
    )
        .appendInherited(
            new KeyedCodec<>("Dir", Codec.STRING, false),
            (o, v) -> {
                final var dir = v.trim().toLowerCase();
                if (
                    dir.equals("none") ||
                    dir.equals("up") ||
                    dir.equals("down") ||
                    dir.equals("left") ||
                    dir.equals("right") ||
                    dir.equals("front") ||
                    dir.equals("back")
                ) {
                    o.direction = dir;
                }
            },
            o -> o.direction,
            (o, p) -> o.direction = p.direction
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Offset", Codec.INT_ARRAY, false),
            (o, v) -> {
                if (v.length != 3) {
                    return;
                }
                o.offset = new Vector3i(v[0], v[1], v[2]);
            },
            o -> {
                int[] offsets = { o.offset.x, o.offset.y, o.offset.z };
                return offsets;
            },
            (o, p) -> o.offset = p.offset
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("ItemId", Codec.STRING, false),
            (o, v) -> {
                if (v == null) {
                    return;
                }

                o.itemId = v;
            },
            o -> o.itemId,
            (o, p) -> o.itemId = p.itemId
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("At", Codec.INT_ARRAY, false),
            (o, v) -> {
                if (v.length != 3) {
                    return;
                }
                o.at = new Vector3i(v[0], v[1], v[2]);
            },
            o -> {
                if (o.at == null) {
                    return null;
                }

                final int[] coords = { o.at.x, o.at.y, o.at.z };
                return coords;
            },
            (o, p) -> o.at = p.at
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Quantity", Codec.INTEGER, false),
            (o, v) -> {
                if (v == null || v < 1) {
                    return;
                }

                o.quantity = v;
            },
            o -> o.quantity,
            (o, p) -> o.quantity = p.quantity
        )
        .add()
        .build();

    @Nonnull
    public static final Vector3i getDirectionOffset(final @Nonnull String direction) {
        switch (direction) {
            case "down":
                return new Vector3i(0, -1, 0);
            case "right":
                return new Vector3i(1, 0, 0);
            case "left":
                return new Vector3i(-1, 0, 0);
            case "front":
                return new Vector3i(0, 0, 1);
            case "back":
                return new Vector3i(0, 0, -1);
            case "up":
                return new Vector3i(0, 1, 0);
            default:
                return new Vector3i(0, 0, 0);
        }
    }

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
        final Vector3i coords;
        if (this.at != null) {
            coords = this.at.add(getDirectionOffset(this.direction)).add(this.offset);
        } else {
            final var targetBlockPos = interactionContext.getTargetBlock();
            if (targetBlockPos == null) {
                return;
            }

            coords = new Vector3i(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z)
                .add(getDirectionOffset(this.direction))
                .add(this.offset);
        }

        ItemUtils.spawn(playerRef, commandBuffer, coords, new ItemStack(this.itemId, this.quantity));
    }

    @Nonnull
    @SuppressWarnings("null")
    public final String getId() {
        return this.getClass().getName();
    }

    @Nonnull
    public final BuilderCodec<SpawnItemInteraction> getCodec() {
        return SpawnItemInteraction.CODEC;
    }

    protected Assets<Interaction, ?> registerToPlugin(final @Nonnull HytalePlugin plugin) {
        return plugin.getCodecRegistry(Interaction.CODEC).register(this.getId(), this.getClass(), this.getCodec());
    }
}
