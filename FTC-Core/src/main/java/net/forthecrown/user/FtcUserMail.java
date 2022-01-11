package net.forthecrown.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

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
    public void add(Component message, @Nullable UUID sender) {
        mail.add(0, new MailMessage(message, sender, System.currentTimeMillis()));
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
    public void clear() {
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
    public void deserialize(JsonElement element) {
        mail.clear();
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

            if(!messageTooOld(message)) mail.add(message);
        }
    }

    boolean messageTooOld(MailMessage message) {
        return (System.currentTimeMillis() - message.sent) > TimeUtil.MONTH_IN_MILLIS;
    }

    @Override
    public JsonElement serialize() {
        if(ListUtils.isNullOrEmpty(mail)) return null;

        JsonArray array = new JsonArray();

        for (MailMessage m: getMail()) {
            if(messageTooOld(m)) continue;

            JsonWrapper json = JsonWrapper.empty();

            json.add("msg", ChatUtils.toJson(m.message));
            json.add("sent", m.sent);

            if(m.read) json.add("read", true);
            if(m.sender != null) json.addUUID("sender", m.sender);

            array.add(json.getSource());
        }

        return array;
    }
}
