package net.forthecrown.core.serialization;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    JsonElement serialize();
}
