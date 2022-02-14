package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentRecord implements JsonSerializable, ComponentLike {
    public final PunishmentType type;
    public final String punisher;
    public final String reason;
    public final String extra;
    public final long date;
    public final long expiresAt;

    public PunishmentRecord(PunishmentType type, CommandSource punisher, String reason, long date, long length) {
        this(type, punisher, reason, null, date, length);
    }

    public PunishmentRecord(PunishmentType type, CommandSource punisher, String reason, String extra, long date, long expiresAt) {
        this.type = type;
        this.punisher = punisher.textName();
        this.reason = reason;
        this.extra = extra;
        this.date = date;
        this.expiresAt = expiresAt;
    }

    public PunishmentRecord(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        type = JsonUtils.readEnum(PunishmentType.class, json.get("type"));
        punisher = json.get("punisher").getAsString();
        date = json.get("date").getAsLong();

        JsonElement extra = json.get("extra");
        if(extra != null && extra.isJsonPrimitive()) this.extra = extra.getAsString();
        else this.extra = null;

        JsonElement r = json.get("reason");
        if(r != null && r.isJsonPrimitive()) this.reason = r.getAsString();
        else this.reason = null;

        JsonElement length = json.get("expiresAt");
        if(length == null || length.isJsonNull()) this.expiresAt = -1;
        else this.expiresAt = length.getAsLong();
    }

    public boolean hasReason(){
        return !FtcUtils.isNullOrBlank(reason);
    }

    public boolean isPermanent(){
        return expiresAt == -1;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        
        json.add("punisher", new JsonPrimitive(punisher));
        json.add("date", new JsonPrimitive(date));
        json.add("type", new JsonPrimitive(type.toString()));
        if(expiresAt > 0) json.add("expriesAt", new JsonPrimitive(expiresAt));
        if(reason != null) json.add("reason", new JsonPrimitive(reason));

        return json;
    }

    @Override
    public @NonNull Component asComponent() {
        return Component.text("  Punishment on ")
                .append(FtcFormatter.formatDate(date))
                .append(Component.text(':'))
                .append(Component.newline())
                .append(Component.text("   Punisher: ")
                        .append(Component.text(punisher).color(NamedTextColor.WHITE))
                        .color(NamedTextColor.YELLOW)
                )
                .append(Component.newline())
                .append(Component.text("   Type: ")
                        .append(Component.text(type.name().toLowerCase()).color(NamedTextColor.WHITE))
                        .color(NamedTextColor.YELLOW)
                )
                .append(
                        expiresAt > 0 ?
                                Component.newline()
                                        .append(Component.text("   Expires: ").color(NamedTextColor.YELLOW))
                                        .append(FtcFormatter.formatDate(expiresAt)).color(NamedTextColor.WHITE)
                                : Component.empty()
                );
    }
}