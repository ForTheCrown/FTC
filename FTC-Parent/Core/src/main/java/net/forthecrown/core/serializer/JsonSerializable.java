package net.forthecrown.core.serializer;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    JsonElement serialize();
}
