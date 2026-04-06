package dev.twunk.hytale.utils;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import javax.annotation.Nullable;

/**
 * For displaying notifications (lil GUI popups in the bottom right ish)
 */
public class Toast extends NotificationUtil {

    public Message message;

    @Nullable
    public Message secondaryMessage;

    @Nullable
    public String icon;

    @Nullable
    public ItemWithAllMetadata item;

    public NotificationStyle style;

    public Toast(final @Nullable Object message) {
        this.message = Chat.parse(message);
        this.style = NotificationStyle.Default;
    }

    public Toast() {
        this.message = Message.empty();
        this.style = NotificationStyle.Default;
    }

    public static Toast build() {
        return new Toast();
    }

    public static Toast build(final @Nullable Object message) {
        return new Toast(Chat.parse(message));
    }

    public static Toast build(final Object message, final Object secondaryMessage) {
        return new Toast(message).setSecondaryMessage(secondaryMessage);
    }

    public Toast setMessage(final @Nullable Object message) {
        this.message = Chat.parse(message);
        return this;
    }

    public Toast setPrimaryMessage(final @Nullable Object message) {
        this.message = Chat.parse(message);
        return this;
    }

    public Toast setSecondaryMessage(final @Nullable Object secondaryMessage) {
        if (secondaryMessage == null) {
            this.secondaryMessage = null;
        } else {
            this.secondaryMessage = Chat.parse(secondaryMessage);
        }
        return this;
    }

    public Toast setIcon(final @Nullable String icon) {
        this.icon = icon;
        return this;
    }

    public Toast setItem(final @Nullable ItemWithAllMetadata item) {
        this.item = item;
        return this;
    }

    public Toast setStyle(final @Nullable NotificationStyle style) {
        if (style == null) {
            this.style = NotificationStyle.Default;
        } else {
            this.style = style;
        }
        return this;
    }

    public void send() {
        Toast.sendNotificationToUniverse(this.message, this.secondaryMessage, this.icon, this.item, this.style);
    }

    public void send(PacketHandler handler) {
        Toast.sendNotification(handler, this.message, this.secondaryMessage, this.icon, this.item, this.style);
    }

    public void send(Player player) {
        Toast.sendNotificationToPlayer(player, this.message, this.secondaryMessage, this.icon, this.item, this.style);
    }

    public void send(World world) {
        for (PlayerRef playerRefComponent : world.getPlayerRefs()) {
            this.send(playerRefComponent.getPacketHandler());
        }
    }

    public static void sendNotificationToPlayer(
        final Player player,
        final Message message,
        final @Nullable Message secondaryMessage,
        final @Nullable String icon,
        final @Nullable ItemWithAllMetadata item,
        final NotificationStyle style
    ) {
        final var ref = player.getReference();
        if (ref == null) {
            // failed to get player reference
            return;
        }

        final var playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        Toast.sendNotification(playerRef.getPacketHandler(), message, secondaryMessage, icon, item, style);
    }
}
