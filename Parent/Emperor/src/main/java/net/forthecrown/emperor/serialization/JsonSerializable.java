package net.forthecrown.emperor.serialization;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    JsonElement serialize();
}
