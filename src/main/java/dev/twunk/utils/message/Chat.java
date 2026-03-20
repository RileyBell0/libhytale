package dev.twunk.utils.message;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Chat {

    private static final Message INFO_PREFIX = Message.raw("[INFO] ").bold(true).color("#0083db").monospace(true);

    @Nonnull
    public static final Message parse(@Nullable final Object message) {
        if (message == null) {
            return Message.empty();
        }

        if (message instanceof Message) {
            return (Message) message;
        } else if (message instanceof String) {
            return Message.raw((String) message);
        } else {
            var strVal = message.toString();
            if (strVal == null) {
                return Message.empty();
            } else {
                return Message.raw(strVal);
            }
        }
    }

    @Nonnull
    public static final Message join(@Nullable final Object... messages) {
        if (messages == null) {
            return Message.empty();
        }

        final List<Message> parsed = new ArrayList<>();
        for (var message : messages) {
            parsed.add(Chat.parse(message));
        }

        Message[] asArray = parsed.toArray(new Message[0]);
        if (asArray == null) {
            return Message.empty();
        }

        return Message.join(asArray);
    }

    @Nonnull
    private static final Message constructLogMessage(@Nonnull final Message message) {
        return Message.join(INFO_PREFIX, message.monospace(true)).monospace(true);
    }

    public static void sendMessage(@Nonnull final Player player, @Nonnull final Object... messages) {
        player.sendMessage(Chat.join(messages));
    }

    public static void log(@Nonnull final Player player, @Nonnull final Object... messages) {
        player.sendMessage(constructLogMessage(Chat.join(messages)));
    }

    // defaults to log to the universe
    public static void log(@Nonnull final World world, @Nonnull final Object... messages) {
        world.sendMessage(constructLogMessage(Chat.join(messages)));
    }

    // defaults to log to the universe
    public static void log(@Nonnull final Object... messages) {
        Universe.get().sendMessage(constructLogMessage(Chat.join(messages)));
    }
}
