package net.forthecrown.serializer;

import com.google.gson.JsonElement;

public interface JsonDeserializable {
    /**
     * Deserializes from the given json element
     * @param element the element to load from
     */
    void deserialize(JsonElement element);
}
