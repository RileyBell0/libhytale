package dev.twunk.utils.message;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Toast extends NotificationUtil {

    @Nonnull
    public Message message;

    @Nullable
    public Message secondaryMessage;

    @Nullable
    public String icon;

    @Nullable
    public ItemWithAllMetadata item;

    @Nonnull
    public NotificationStyle style;

    public Toast(@Nullable final Object message) {
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

    public static Toast build(@Nullable final Object message) {
        return new Toast(Chat.parse(message));
    }

    public static Toast build(@Nonnull final Object message, @Nonnull final Object secondaryMessage) {
        return new Toast(message).setSecondaryMessage(secondaryMessage);
    }

    @Nonnull
    public Toast setMessage(@Nullable final Object message) {
        this.message = Chat.parse(message);
        return this;
    }

    @Nonnull
    public Toast setPrimaryMessage(@Nullable final Object message) {
        this.message = Chat.parse(message);
        return this;
    }

    @Nonnull
    public Toast setSecondaryMessage(@Nullable final Object secondaryMessage) {
        if (secondaryMessage == null) {
            this.secondaryMessage = null;
        } else {
            this.secondaryMessage = Chat.parse(secondaryMessage);
        }
        return this;
    }

    @Nonnull
    public Toast setIcon(@Nullable final String icon) {
        this.icon = icon;
        return this;
    }

    @Nonnull
    public Toast setItem(@Nullable final ItemWithAllMetadata item) {
        this.item = item;
        return this;
    }

    @Nonnull
    public Toast setStyle(@Nullable final NotificationStyle style) {
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

    public void send(@Nonnull PacketHandler handler) {
        Toast.sendNotification(handler, this.message, this.secondaryMessage, this.icon, this.item, this.style);
    }

    public void send(@Nonnull Player player) {
        Toast.sendNotificationToPlayer(player, this.message, this.secondaryMessage, this.icon, this.item, this.style);
    }

    public void send(@Nonnull World world) {
        for (PlayerRef playerRefComponent : world.getPlayerRefs()) {
            this.send(playerRefComponent.getPacketHandler());
        }
    }

    public static void sendNotificationToPlayer(
        @Nonnull final Player player,
        @Nonnull final Message message,
        @Nullable final Message secondaryMessage,
        @Nullable final String icon,
        @Nullable final ItemWithAllMetadata item,
        @Nonnull final NotificationStyle style
    ) {
        var ref = player.getReference();
        if (ref == null) {
            // failed to get player reference
            return;
        }

        var playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        Toast.sendNotification(playerRef.getPacketHandler(), message, secondaryMessage, icon, item, style);
    }
}
