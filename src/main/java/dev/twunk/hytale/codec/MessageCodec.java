package dev.twunk.hytale.codec;

import com.hypixel.hytale.server.core.Message;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import javax.annotation.Nullable;

/**
 * Logs can be built up of several messages all concatenated together. This
 * the codec for a single message. see the overall log codec to understand
 * where to use this
 */
@Serializable(documentation = "Debug interaction that sends a message on use.")
public class MessageCodec {

    /**
     * Your overarching message itself
     */
    @Serialize
    @SuppressWarnings("CanBeFinal")
    private String message = "";

    /**
     * OPTIONAL
     * The text color of the message (as hex string, e.g. "#cacaca")
     */
    @Serialize
    @SuppressWarnings("CanBeFinal")
    private @Nullable String color = null;

    /**
     * OPTIONAL
     * A link to associate with your message
     */
    @Serialize
    @SuppressWarnings("CanBeFinal")
    private @Nullable String link = null;

    /**
     * OPTIONAL
     * If your message should be bold
     */
    @Serialize
    @SuppressWarnings("CanBeFinal")
    private boolean bold = false;

    /**
     * OPTIONAL
     * If your message should be italic
     */
    @Serialize
    @SuppressWarnings("CanBeFinal")
    private boolean italic = false;

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    public Message toMessage() {
        var msg = Message.raw(this.message).bold(this.bold).italic(this.italic);
        if (this.color != null) {
            msg = msg.color(this.color);
        }
        if (this.link != null) {
            msg.link(this.link);
        }

        return msg;
    }
}
