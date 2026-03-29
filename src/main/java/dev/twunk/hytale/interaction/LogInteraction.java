package dev.twunk.hytale.interaction;

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
import dev.twunk.annotations.Serialize;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.hytale.utils.AutoCodecGenerator;
import dev.twunk.hytale.utils.Chat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An interaction that when run simply logs to the chat the message you included
 * with the severity you specified.
 *
 * Useful for light printf debugging interactions in-game
 */
public class LogInteraction extends SimpleInstantInteraction {

    /**
     * Logs can be built up of several messages all concatenated together. This
     * the codec for a single message. see the overall log codec to understand
     * where to use this
     */
    @Nonnull
    public static final BuilderCodec<LogInteraction> SINGLE_MESSAGE_CODEC = AutoCodecGenerator.process(
        LogInteraction.class,
        LogInteraction::new
    )
        .documentation("Debug interaction that sends a message on use.")
        .build();

    /**
     * The overall log itself
     * Suppressing "null" from `Level.INFO`
     */
    @SuppressWarnings("null")
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
            new KeyedCodec<>("Level", Codec.STRING, false),
            (o, v) -> {
                final var level = Level.parse(v);
                if (level == null) {
                    o.level = Level.INFO;
                } else {
                    o.level = level;
                }
            },
            o -> o.level == null ? null : o.level.toString(),
            (o, p) -> o.level = p.level
        )
        .add()
        .build();

    /**
     * All your messages (if you provided multiple).
     * They'll be concatenated with Message.join before printing them
     */
    private ArrayList<Message> messages = new ArrayList<>();

    /**
     * Your overarching message itself
     */
    @Serialize(required = true)
    private String message;

    /**
     * OPTIONAL
     * The text color of the message (as hex string, e.g. "#cacaca")
     */
    @Serialize
    private @Nullable String color = null;

    /**
     * OPTIONAL
     * A link to associate with your message
     */
    @Serialize
    private @Nullable String link = null;

    /**
     * OPTIONAL
     * If your message should be bold
     */
    @Serialize
    private boolean bold = false;

    /**
     * OPTIONAL
     * If your message should be italic
     */
    @Serialize
    private boolean italic = false;

    /**
     * Severity/level of your message. We'll prefix [LEVEL]
     * SEVERE | WARNING | INFO | CONFIG | FINE | FINER | FINEST
     */
    @SuppressWarnings("null")
    private @Nonnull Level level = Level.INFO;

    @Nonnull
    @SuppressWarnings("null")
    public final String getId() {
        return this.getClass().getName();
    }

    @Nonnull
    public final BuilderCodec<LogInteraction> getCodec() {
        return LogInteraction.CODEC;
    }

    protected Assets<Interaction, ?> registerToPlugin(final @Nonnull HytalePlugin plugin) {
        return plugin.getCodecRegistry(Interaction.CODEC).register(this.getId(), this.getClass(), this.getCodec());
    }

    /**
     * Runs the interaction.
     * This one's designed to be part of an interaction workflow, to effectively 'printf'
     * debug your way through. Chuck it in part of any row of interactions and it'll log some
     * stuff for you.
     *
     * SO. this writes a message to the chat for the player that started the interaction
     */
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

        Chat.log(msg);
    }
}
