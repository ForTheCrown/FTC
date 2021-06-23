package net.forthecrown.serializer;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    JsonElement serialize();
}
