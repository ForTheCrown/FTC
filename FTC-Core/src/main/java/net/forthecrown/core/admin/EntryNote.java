package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;

/**
 * A single staff note
 */
public record EntryNote(String info, long issued, String source) implements JsonSerializable {
    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("content", info);
        json.add("issued", issued);
        json.add("source", source);

        return json.getSource();
    }

    public static EntryNote read(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new EntryNote(
                json.getString("content"),
                json.getLong("issued"),
                json.getString("source")
        );
    }
}