package net.forthecrown.utils;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    /**
     * Serializes the object into Json
     * @return The json representation of this object
     */
    JsonElement serialize();
}