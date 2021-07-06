package net.forthecrown.serializer;

import com.google.gson.JsonElement;

public interface JsonDeserializable {
    void deserialize(JsonElement element);
}
