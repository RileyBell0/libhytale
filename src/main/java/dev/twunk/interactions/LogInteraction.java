package dev.twunk.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry.Assets;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.utils.message.Chat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LogInteraction extends SimpleInstantInteraction {

    @Nonnull
    public static final BuilderCodec<LogInteraction> SINGLE_MESSAGE_CODEC = BuilderCodec.builder(
        LogInteraction.class,
        LogInteraction::new,
        SimpleInstantInteraction.CODEC
    )
        .documentation("Debug interaction that sends a message on use.")
        .appendInherited(
            new KeyedCodec<>("Message", Codec.STRING, true),
            (interaction, s) -> interaction.message = s,
            interaction -> interaction.message,
            (interaction, parent) -> interaction.message = parent.message
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Color", Codec.STRING, false),
            (o, v) -> o.color = v,
            o -> o.color,
            (o, p) -> o.color = p.color
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Link", Codec.STRING, false),
            (o, v) -> o.link = v,
            o -> o.link,
            (o, p) -> o.link = p.link
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Bold", Codec.BOOLEAN, false),
            (o, v) -> o.bold = (v != null && v.equals(true)),
            o -> o.bold,
            (o, p) -> o.bold = p.bold
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Italic", Codec.BOOLEAN, false),
            (o, v) -> o.italic = (v != null && v.equals(true)),
            o -> o.italic,
            (o, p) -> o.italic = p.italic
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("IsLog", Codec.BOOLEAN, false),
            (o, v) -> o.isLog = (v == null || (v != null && v.equals(false))),
            o -> o.isLog,
            (o, p) -> o.isLog = p.isLog
        )
        .add()
        .build();

    @Nonnull
    public static final BuilderCodec<LogInteraction> CODEC = BuilderCodec.builder(
        LogInteraction.class,
        LogInteraction::new,
        SimpleInstantInteraction.CODEC
    )
        .documentation("Debug interaction that sends a message on use.")
        .append(
            new KeyedCodec<>("Messages", new ArrayCodec<>(Message.CODEC, Message[]::new)),
            (self, messages) -> {
                self.messages.addAll(Arrays.asList(messages));
            },
            self -> self.messages.toArray(Message[]::new)
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Message", Codec.STRING, true),
            (interaction, s) -> interaction.message = s,
            interaction -> interaction.message,
            (interaction, parent) -> interaction.message = parent.message
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Color", Codec.STRING, false),
            (o, v) -> o.color = v,
            o -> o.color,
            (o, p) -> o.color = p.color
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Level", Codec.STRING, false),
            (o, v) -> {
                final var level = Level.parse(v);
                if (level == null) {
                    o.level = Level.INFO;
                } else {
                    o.level = level;
                }
            },
            o -> o.level.toString(),
            (o, p) -> o.level = p.level
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Link", Codec.STRING, false),
            (o, v) -> o.link = v,
            o -> o.link,
            (o, p) -> o.link = p.link
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Bold", Codec.BOOLEAN, false),
            (o, v) -> o.bold = (v != null && v.equals(true)),
            o -> o.bold,
            (o, p) -> o.bold = p.bold
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("Italic", Codec.BOOLEAN, false),
            (o, v) -> o.italic = (v != null && v.equals(true)),
            o -> o.italic,
            (o, p) -> o.italic = p.italic
        )
        .add()
        .appendInherited(
            new KeyedCodec<>("IsLog", Codec.BOOLEAN, false),
            (o, v) -> o.isLog = (v == null || (v != null && v.equals(false))),
            o -> o.isLog,
            (o, p) -> o.isLog = p.isLog
        )
        .add()
        .build();

    private ArrayList<Message> messages = new ArrayList<>();
    private String message;
    private String color = null;
    private String link = null;
    private boolean bold = false;
    private boolean italic = false;

    // "SEVERE";
    // "WARNING";
    // "INFO";
    // "CONFIG";
    // "FINE";
    // "FINER";
    // "FINEST";
    private Level level = Level.INFO;

    private boolean isLog = true;

    @Nonnull
    @SuppressWarnings("null")
    public final String getId() {
        return this.getClass().getName();
    }

    @Nonnull
    public final BuilderCodec<LogInteraction> getCodec() {
        return LogInteraction.CODEC;
    }

    protected Assets<Interaction, ?> registerToPlugin(final @Nonnull ModPlugin plugin) {
        return plugin.getCodecRegistry(Interaction.CODEC).register(this.getId(), this.getClass(), this.getCodec());
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

        Message msg;
        final var messages = this.messages.toArray(Message[]::new);
        if (messages == null) {
            msg = Chat.parse(message);
        } else {
            msg = Message.join(Chat.parse(message), Message.join(messages));
        }

        if (this.color != null) {
            msg = msg.color(this.color);
        }
        if (this.link != null) {
            msg = msg.link(this.link);
        }
        msg = msg.bold(this.bold);
        msg = msg.italic(this.italic);

        if (this.isLog) {
            Chat.log(msg);
        } else {
            Chat.send(msg);
        }
    }
}
