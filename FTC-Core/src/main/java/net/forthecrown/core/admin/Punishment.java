package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Objects;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

public final class Punishment implements JsonSerializable {
    private final String source;
    private final String reason;
    private final String extra;
    private final PunishType type;
    private final long began;
    private final long expires;

    private BukkitTask task;

    public Punishment(
            String source,
            String reason,
            String extra,
            PunishType type,
            long began,
            long expires
    ) {
        this.source = source;
        this.reason = reason;
        this.extra = extra;
        this.type = type;
        this.began = began;
        this.expires = expires;
    }

    public void startTask(Runnable callback) {
        if(expires == INDEFINITE_EXPIRY) return;
        cancelTask();

        long until = expires - System.currentTimeMillis();
        until = TimeUtil.millisToTicks(until);

        Bukkit.getScheduler().runTaskLater(Crown.inst(), callback, until);
    }

    public void cancelTask() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
        task = null;
    }

    public boolean willExpire() {
        return expires != INDEFINITE_EXPIRY;
    }

    public String source() {
        return source;
    }

    public String reason() {
        return reason;
    }

    public PunishType type() {
        return type;
    }

    public String extra() {
        return extra;
    }

    public long began() {
        return began;
    }

    public long expires() {
        return expires;
    }

    public void writeDisplay(ComponentWriter writer) {
        writeField(writer, "Source", source);
        writeField(writer, "Began", FtcFormatter.formatDate(began));
        writeField(writer, "Type", type.presentableName());

        if(expires != INDEFINITE_EXPIRY) {
            writeField(writer, "Expires", FtcFormatter.formatDate(expires));
        }

        writeField(writer, "Reason", reason);
        writeField(writer, "Extra", extra);
    }

    private void writeField(ComponentWriter writer, String field, @Nullable String val) {
        if(FtcUtils.isNullOrBlank(val)) return;
        writeField(writer, field, Component.text(val));
    }

    private void writeField(ComponentWriter writer, String field, @Nullable Component display) {
        if(display == null) return;
        writer.write(Component.text(field + ": ", NamedTextColor.GRAY));
        writer.write(display);
        writer.newLine();
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("source", source);
        json.addDate("began", new Date(began));
        json.addEnum("type", type);

        if (!FtcUtils.isNullOrBlank(reason)) {
            json.add("reason", reason);
        }

        if (FtcUtils.isNullOrBlank(extra)) {
            json.add("extra", extra);
        }

        if (expires != INDEFINITE_EXPIRY) {
            json.addDate("expires", new Date(expires));
        }

        return json.getSource();
    }

    public static Punishment read(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new Punishment(
                json.getString("source"),
                json.getString("reason", null),
                json.getString("extra", null),
                json.getEnum("type", PunishType.class, PunishType.SOFT_MUTE),
                json.getDate("began").getTime(),
                json.getDate("expires", new Date(INDEFINITE_EXPIRY)).getTime()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Punishment) obj;
        return Objects.equals(this.source, that.source) &&
                Objects.equals(this.reason, that.reason) &&
                Objects.equals(this.type, that.type) &&
                this.began == that.began &&
                this.expires == that.expires;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, reason, type, began, expires);
    }

    @Override
    public String toString() {
        return "Punishment[" +
                "source=" + source + ", " +
                "reason=" + reason + ", " +
                "type=" + type + ", " +
                "began=" + began + ", " +
                "expires=" + expires + ']';
    }

}