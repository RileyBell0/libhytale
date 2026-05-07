package dev.twunk.hytale.utils;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.twunk.hytale.codec.MessageCodec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * for writing messages to the chat. Scoped.
 * - universe OR
 * - world OR
 * - player
 */
public abstract class Chat {

    private static final String DEFAULT_COLOR = "#00d9ed";

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static String getColor(final @Nullable Level level) {
        if (level == null) {
            return DEFAULT_COLOR;
        }

        int val = level.intValue();
        return switch (val) {
            case Integer c when c <= Level.ALL.intValue() -> "#dadada";
            case Integer c when c <= Level.FINEST.intValue() -> "#a600ed";
            case Integer c when c <= Level.FINER.intValue() -> "#5b00ed";
            case Integer c when c <= Level.FINE.intValue() -> "#002fed";
            case Integer c when c <= Level.CONFIG.intValue() -> "#0096ed";
            case Integer c when c <= Level.INFO.intValue() -> DEFAULT_COLOR;
            case Integer c when c <= Level.WARNING.intValue() -> "#edba00";
            case Integer c when c <= Level.SEVERE.intValue() -> "#c90d00";
            default -> DEFAULT_COLOR;
        };
    }

    @SuppressWarnings("null")
    public static Message parse(final @Nullable Object toMessage) {
        return switch (toMessage) {
            case Message m -> m;
            case String m -> Message.raw(m);
            case MessageCodec codec -> codec.toMessage();
            case Object unknown -> Message.raw(unknown.toString());
            case null -> Message.empty();
        };
    }

    public static Message join(final @Nullable Object... messages) {
        if (messages == null) {
            return Message.empty();
        }

        final List<Message> parsed = new ArrayList<>();
        for (var message : messages) {
            parsed.add(Chat.parse(message));
        }

        Message[] asArray = parsed.toArray(Message[]::new);
        if (asArray == null) {
            return Message.empty();
        }

        return Message.join(asArray);
    }

    private static Message constructLogMessage(final Message message) {
        return constructLogMessage(Level.INFO, message);
    }

    private static Message constructLogMessage(@Nullable Level level, final Message message) {
        if (level == null) {
            level = Level.INFO;
        }

        final var prefix = Message.raw("[" + level + "] ").color(getColor(level)).bold(true).monospace(true);

        return Message.join(prefix, message.monospace(true)).monospace(true);
    }

    public static void send(final Player player, final Object... messages) {
        player.sendMessage(Chat.join(messages));
    }

    public static void log(final Player player, final Object... messages) {
        player.sendMessage(constructLogMessage(Chat.join(messages)));
    }

    public static void log(final Player player, final Level level, final Object... messages) {
        player.sendMessage(constructLogMessage(level, Chat.join(messages)));
    }

    public static void send(final World world, final Object... messages) {
        world.sendMessage(Chat.join(messages));
    }

    public static void log(final World world, final Object... messages) {
        world.sendMessage(constructLogMessage(Chat.join(messages)));
    }

    public static void log(final World world, final Level level, final Object... messages) {
        world.sendMessage(constructLogMessage(level, Chat.join(messages)));
    }

    public static void send(final Object... messages) {
        Universe.get().sendMessage(Chat.join(messages));
    }

    public static void log(final Object... messages) {
        Universe.get().sendMessage(constructLogMessage(Chat.join(messages)));
    }

    public static void log(final Level level, final Object... messages) {
        Universe.get().sendMessage(constructLogMessage(level, Chat.join(messages)));
    }
}
