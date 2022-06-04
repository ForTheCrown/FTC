package net.forthecrown.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.TimeUtil;

import java.util.UUID;

public class FtcUserMail extends AbstractUserAttachment implements UserMail {
    private final ObjectList<MailMessage> mail = new ObjectArrayList<>();

    public FtcUserMail(FtcUser user) {
        super(user, "mail");
    }

    @Override
    public boolean canSee(UUID id) {
        return getUser().getUniqueId().equals(id);
    }

    @Override
    public void remove(int index) {
        mail.remove(index);
    }

    @Override
    public void readAll() {
        for (MailMessage m: mail) {
            m.read = true;
        }
    }

    @Override
    public void clearPartial() {
        mail.removeIf(message -> !UserMail.hasAttachment(message) || message.attachmentClaimed);
    }

    @Override
    public void clearTotal() {
        mail.clear();
    }

    @Override
    public int size() {
        return mail.size();
    }

    @Override
    public ObjectList<MailMessage> getUnread() {
        ObjectList<MailMessage> unread = new ObjectArrayList<>();

        for (MailMessage m: mail) {
            if(m.read) continue;
            unread.add(m);
        }

        return unread;
    }

    @Override
    public ObjectList<MailMessage> getMail() {
        return mail;
    }

    @Override
    public MailMessage get(int index) throws IndexOutOfBoundsException {
        return mail.get(index);
    }

    @Override
    public int indexOf(MailMessage message) {
        return mail.indexOf(message);
    }

    @Override
    public void add(MailMessage message) {
        mail.add(0, message);
    }

    @Override
    public void deserialize(JsonElement element) {
        clearTotal();
        if(element == null) return;

        JsonArray array = element.getAsJsonArray();

        for (JsonElement e: array) {
            JsonWrapper json = JsonWrapper.of(e.getAsJsonObject());

            MailMessage message = new MailMessage(
                    ChatUtils.fromJson(json.get("msg")),
                    json.getUUID("sender"),
                    json.getLong("sent"),
                    json.has("read")
            );

            if(json.has("attachment")) {
                message.attachmentClaimed = json.getBool("attachment_claimed");
                message.attachment = MailAttachment.load(json.get("attachment"));
            }

            if(shouldRetainMessage(message)) mail.add(message);
        }
    }

    boolean shouldRetainMessage(MailMessage message) {
        if (UserMail.hasAttachment(message)) {
            return true;
        }

        return !TimeUtil.hasCooldownEnded(FtcVars.dataRetentionTime.get(), message.sent);
    }

    @Override
    public JsonElement serialize() {
        if(ListUtils.isNullOrEmpty(mail)) return null;

        JsonArray array = new JsonArray();

        for (MailMessage m: getMail()) {
            if(!shouldRetainMessage(m)) continue;

            JsonWrapper json = JsonWrapper.empty();

            json.add("msg", ChatUtils.toJson(m.message));
            json.add("sent", m.sent);

            if(m.read) json.add("read", true);
            if(m.sender != null) json.addUUID("sender", m.sender);

            if (UserMail.hasAttachment(m)) {
                json.add("attachment_claimed", m.attachmentClaimed);
                json.add("attachment", m.attachment.serialize());
            }

            array.add(json.getSource());
        }

        return array;
    }
}