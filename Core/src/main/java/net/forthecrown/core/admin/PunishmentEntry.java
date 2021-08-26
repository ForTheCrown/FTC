package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class PunishmentEntry implements JsonSerializable {

    public final UUID id;
    private final List<PunishmentRecord> records = new ArrayList<>();
    private final Map<PunishmentType, PunishmentRecord> current = new HashMap<>();

    //Create new
    public PunishmentEntry(UUID id) {
        this.id = id;
    }

    //Deserialize existing from json
    public PunishmentEntry(UUID id, JsonElement element){
        this.id = id;
        JsonObject json = element.getAsJsonObject();

        JsonElement currentElement = json.get("current");
        if(currentElement != null && currentElement.isJsonArray()){
            for (JsonElement e: currentElement.getAsJsonArray()){
                PunishmentRecord record = new PunishmentRecord(e);
                current.put(record.type, record);
            }
        }

        JsonElement arrayElement = json.get("records");
        if(arrayElement == null || arrayElement.isJsonArray()) return;

        for (JsonElement e: arrayElement.getAsJsonArray()){
            records.add(new PunishmentRecord(e));
        }
    }

    //Check if they've received the given punishment
    public boolean checkPunished(PunishmentType type){
        if(!current.containsKey(type)) return false;
        PunishmentRecord record = current.get(type);

        if(record.expiresAt == -1) return true;
        if(record.expiresAt < System.currentTimeMillis()){
            current.remove(type);
            return false;
        }

        return true;
    }

    //Punish them with the given record
    public void punish(PunishmentRecord record){
        records.add(record);
        current.put(record.type, record);
    }

    //Pardon the given type of punishment
    public void pardon(PunishmentType type) throws CommandSyntaxException {
        if(!current.containsKey(type)) throw FtcExceptionProvider.create("User has not received the given punishment");
        current.remove(type);
    }

    //Gets a currently active punishment from the given type
    public PunishmentRecord getCurrent(PunishmentType type){
        return current.get(type);
    }

    //Displays all current and past punishments, used by /profile
    public Component display(boolean past){
        TextComponent.Builder builder = Component.text()
                .append(Component.text(" " + (past ? "P" : "Current p") + "unishments:").color(NamedTextColor.YELLOW));

        if(current.size() > 0){
            if(past){
                builder
                        .append(Component.newline())
                        .append(Component.text("  Current: "));
            }

            current.values().forEach(r -> {
                builder
                        .append(Component.newline())
                        .append(r);
            });
        }

        if(records.size() > 0 && past){
            builder
                    .append(Component.newline())
                    .append(Component.text("  Past: "));

            records.forEach(r -> {
                builder
                        .append(Component.newline())
                        .append(r);
            });
        }

        return builder.build();
    }

    @Override //Serialize
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.add("uuid", new JsonPrimitive(id.toString()));
        json.add("current", JsonUtils.writeCollection(current.values(), PunishmentRecord::serialize));
        json.add("records", JsonUtils.writeCollection(records, PunishmentRecord::serialize));

        return json;
    }
}
