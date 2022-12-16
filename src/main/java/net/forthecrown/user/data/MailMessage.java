package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import lombok.Data;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * A small class that holds the data for
 * a mail message, when it was sent and by whom
 * and what it contains.
 */
@Data
public class MailMessage implements JsonSerializable {
    /**
     * The message content of this mail
     */
    private final Component message;

    /**
     * The ID of the user that sent this message,
     * if this was sent by a system message, this
     * will be null
     */
    private final UUID sender;

    /**
     * The timestamp of when this message was sent
     */
    private final long sent;

    /**
     * Whether the message has been read or not
     */
    private boolean read;

    /**
     * A rhine/gem/item attachment this message may
     * hold, will be null if there's no attachment.
     * <p>
     * To stay safe though,use {@link MailAttachment#isEmpty(MailAttachment)}
     * to test if the attachment actually contains any
     * useful data
     */
    private MailAttachment attachment;

    public MailMessage(Component message, UUID sender, long sent, boolean read) {
        this.message = message;
        this.sender = sender;
        this.sent = sent;
        this.read = read;
    }

    public static MailMessage of(Component text) {
        return of(text, null);
    }

    public static MailMessage of(Component text, UUID sender) {
        return of(text, sender, System.currentTimeMillis());
    }

    public static MailMessage of(Component text, UUID sender, long date) {
        return new MailMessage(text, sender, date, false);
    }

    @Override
    public JsonElement serialize() {
        var json = JsonWrapper.create();

        json.addTimeStamp("sent", getSent());

        if (isRead()) {
            json.add("read", true);
        }

        if (getSender() != null) {
            json.addUUID("sender", getSender());
        }

        if (!MailAttachment.isEmpty(attachment)) {
            json.add("attachment", attachment);
        }

        json.addComponent("msg", getMessage());
        return json.getSource();
    }

    public static MailMessage deserialize(JsonElement element) {
        var json = JsonWrapper.wrap(element.getAsJsonObject());

        var message = new MailMessage(
                json.getComponent("msg"),
                json.getUUID("sender"),
                json.getTimeStamp("sent"),
                json.has("read")
        );

        if (json.has("attachment")) {
            message.setAttachment(MailAttachment.load(json.get("attachment")));

            if (json.has("attachment_claimed")) {
                boolean attachmentClaimed = json.getBool("attachment_claimed");
                message.getAttachment().setClaimed(attachmentClaimed);
            }
        }

        return message;
    }
}