package dev.twunk.hytale.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import dev.twunk.annotations.Serializable;
import dev.twunk.annotations.Serialize;
import dev.twunk.lib.AutoBuilderCodec;
import javax.annotation.Nullable;

/**
 * Logs can be built up of several messages all concatenated together. This
 * the codec for a single message. see the overall log codec to understand
 * where to use this
 */
@Serializable(documentation = "Debug interaction that sends a message on use.")
public class MessageCodec {

    public static final BuilderCodec<MessageCodec> CODEC = AutoBuilderCodec.build(MessageCodec.class);

    /**
     * Your overarching message itself
     */
    @Serialize
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
