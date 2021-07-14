package net.forthecrown.serializer;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    /**
     * Serializes the object into Json
     * @return The json representation of this object
     */
    JsonElement serialize();
}
