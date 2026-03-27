package dev.twunk.utils.message;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * for writing messages to the chat. Scoped.
 * - universe OR
 * - world OR
 * - player
 */
public abstract class Chat {

    @Nonnull
    public static final String getColor(final @Nullable Level level) {
        String color = null;
        if (level == null) {
            return "#00d9ed";
        }

        final var levelCode = level.intValue();
        if (levelCode <= Level.ALL.intValue()) {
            color = "#dadada";
        } else if (levelCode <= Level.FINEST.intValue()) {
            color = "#a600ed";
        } else if (levelCode <= Level.FINER.intValue()) {
            color = "#5b00ed";
        } else if (levelCode <= Level.FINE.intValue()) {
            color = "#002fed";
        } else if (levelCode <= Level.CONFIG.intValue()) {
            color = "#0096ed";
        } else if (levelCode <= Level.INFO.intValue()) {
            color = "#00d9ed";
        } else if (levelCode <= Level.WARNING.intValue()) {
            color = "#edba00";
        } else if (levelCode <= Level.SEVERE.intValue()) {
            color = "#c90d00";
        } else {
            color = "#00d9ed";
        }

        return color;
    }

    @Nonnull
    public static final Message parse(final @Nullable Object message) {
        if (message == null) {
            return Message.empty();
        }

        if (message instanceof Message) {
            return (Message) message;
        } else if (message instanceof String) {
            return Message.raw((String) message);
        } else {
            final var strVal = message.toString();
            if (strVal == null) {
                return Message.empty();
            } else {
                return Message.raw(strVal);
            }
        }
    }

    @Nonnull
    public static final Message join(final @Nullable Object... messages) {
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
    private static final Message constructLogMessage(final @Nonnull Message message) {
        return constructLogMessage(Level.INFO, message);
    }

    @Nonnull
    private static final Message constructLogMessage(@Nullable Level level, final @Nonnull Message message) {
        if (level == null) {
            level = Level.INFO;
        }

        if (level == null) {
            return message.monospace(true);
        }

        final var prefix = Message.raw("[" + level.toString() + "] ").color(getColor(level)).bold(true).monospace(true);

        return Message.join(prefix, message.monospace(true)).monospace(true);
    }

    public static void send(final @Nonnull Player player, final @Nonnull Object... messages) {
        player.sendMessage(Chat.join(messages));
    }

    public static void log(final @Nonnull Player player, final @Nonnull Object... messages) {
        player.sendMessage(constructLogMessage(Chat.join(messages)));
    }

    public static void log(
        final @Nonnull Player player,
        final @Nonnull Level level,
        final @Nonnull Object... messages
    ) {
        player.sendMessage(constructLogMessage(level, Chat.join(messages)));
    }

    public static void send(final @Nonnull World world, final @Nonnull Object... messages) {
        world.sendMessage(Chat.join(messages));
    }

    public static void log(final @Nonnull World world, final @Nonnull Object... messages) {
        world.sendMessage(constructLogMessage(Chat.join(messages)));
    }

    public static void log(final @Nonnull World world, final @Nonnull Level level, final @Nonnull Object... messages) {
        world.sendMessage(constructLogMessage(level, Chat.join(messages)));
    }

    public static void send(final @Nonnull Object... messages) {
        Universe.get().sendMessage(Chat.join(messages));
    }

    public static void log(final @Nonnull Object... messages) {
        Universe.get().sendMessage(constructLogMessage(Chat.join(messages)));
    }

    public static void log(final @Nonnull Level level, final @Nonnull Object... messages) {
        Universe.get().sendMessage(constructLogMessage(level, Chat.join(messages)));
    }
}
