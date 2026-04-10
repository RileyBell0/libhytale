package dev.twunk.hytale.interaction;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import dev.twunk.annotations.Serializable;
import dev.twunk.annotations.Serialize;
import dev.twunk.hytale.utils.Chat;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.annotation.Nullable;

/**
 * An interaction that when run simply logs to the chat the message you included
 * with the severity you specified.
 *
 * Useful for light printf debugging interactions in-game
 */
@Serializable(documentation = "Debug interaction that sends a message on use.")
public class LogInteraction extends SimpleInstantInteraction {

    /**
     * All your messages (if you provided multiple).
     * They'll be concatenated with Message.join before printing them
     */
    @Serialize
    private ArrayList<MessageCodec> messages = new ArrayList<>();

    /**
     * Your overarching message itself
     */
    @Serialize(required = true)
    private String message = "";

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
    @Serialize
    @SuppressWarnings("null")
    private Level level = Level.INFO;

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

        Message msg;
        final var messages = this.messages.stream().map(MessageCodec::toMessage).toArray(Message[]::new);
        if (messages == null) {
            msg = Chat.parse(this.message);
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
