package net.forthecrown.emperor.serializer;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    JsonElement serialize();
}
