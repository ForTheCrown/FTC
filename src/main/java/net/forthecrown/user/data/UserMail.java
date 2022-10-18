package net.forthecrown.user.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import net.forthecrown.text.Messages;
import net.forthecrown.core.Vars;
import net.forthecrown.user.ComponentType;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Object that holds a user's mail messages
 */
public class UserMail extends UserComponent {
    /**
     * The list of messages that have been sent to the holder of
     * this mail.
     * <p>
     * New entries will be inserted to the beginning of this list.
     * Old entries, determined with {@link #shouldRetainMessage(MailMessage)},
     * are not serialized or deserialized, but are also not cleared,
     * because I am lazy. - Jules
     */
    @Getter
    private final ObjectList<MailMessage> mail = new ObjectArrayList<>();

    public UserMail(User user, ComponentType<UserMail> type) {
        super(user, type);
    }

    /**
     * Clears all mail messages that either don't have an attachment, or
     * have a claimed attachment.
     * @see MailAttachment
     */
    public void clearPartial() {
        mail.removeIf(message -> MailAttachment.isEmpty(message.getAttachment()) || message.getAttachment().isClaimed());
    }

    /**
     * Adds the given message to this user's mail
     * @param message The message to add
     */
    public void add(MailMessage message) {
        mail.add(0, message);
    }

    /**
     * Adds the given text to this user's mail
     * @param component The message to add
     */
    public void add(Component component) {
        add(component, null);
    }

    /**
     * Adds a message with the given text and sender
     * to this user's mail
     * @param message The message to add
     * @param sender The sender of the message
     */
    public void add(Component message, @Nullable UUID sender) {
        add(MailMessage.of(message, sender));
    }

    /**
     * Tests if the given message should be retained
     * <p>
     * If the message has an attachment that's not claimed
     * then this returns false, otherwise, it tests,
     * if the message has expired, in the sense that is
     * was sent more than {@link Vars#dataRetentionTime}
     * time ago.
     *
     * @param message The message to test
     * @return True, if the message should be retained,
     *         false if it should be deleted
     */
    static boolean shouldRetainMessage(MailMessage message) {
        if (!MailAttachment.isEmpty(message.getAttachment())
                && !message.getAttachment().isClaimed()
        ) {
            return false;
        }

        return !Time.isPast(Vars.dataRetentionTime + message.getSent());
    }

    /**
     * Gets the amount of unread messages
     * @return amount of unread messages
     */
    public int unreadSize() {
        int i = 0;

        for (var m: getMail()) {
            if (m.isRead()) {
                continue;
            }

            i++;
        }

        return i;
    }

    /**
     * Tells the user if they have unread mail
     */
    public void informOfUnread() {
        // If user is offline, we have no one
        // to inform
        if (!getUser().isOnline()) {
            return;
        }

        // If we have no unread
        // mail, then don't display
        int unreadSize = unreadSize();
        if (unreadSize < 1) {
            return;
        }

        var user = getUser();
        user.sendMessage(Messages.mailJoinMessage(unreadSize));
    }

    @Override
    public JsonElement serialize() {
        if (Util.isNullOrEmpty(mail)) {
            return null;
        }

        JsonArray array = new JsonArray();

        for (MailMessage m: getMail()) {
            if (!shouldRetainMessage(m)) {
                continue;
            }

            array.add(m.serialize());
        }

        return array;
    }

    @Override
    public void deserialize(JsonElement element) {
        getMail().clear();

        if (element == null) {
            return;
        }

        JsonArray array = element.getAsJsonArray();

        for (JsonElement e: array) {
            var msg = MailMessage.deserialize(e);

            if (shouldRetainMessage(msg)) {
                add(msg);
            }
        }
    }
}