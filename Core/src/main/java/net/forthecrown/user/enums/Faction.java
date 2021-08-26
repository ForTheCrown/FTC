package net.forthecrown.user.enums;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;

/**
 * A class representing a rank branch
 */
public enum Faction implements JsonSerializable {
    DEFAULT ("Branch-less"),
    ROYALS ("Royals", "Royal"),
    VIKINGS ("Vikings", "Viking"),
    PIRATES ("Pirates", "Pirate");

    private final String name;
    private final String singularName;
    Faction(String name, String singularName){
        this.name = name;
        this.singularName = singularName;
    }
    Faction(String name){
        this.name = name;
        this.singularName = null;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getSingularName() {
        return singularName == null ? name : singularName;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
