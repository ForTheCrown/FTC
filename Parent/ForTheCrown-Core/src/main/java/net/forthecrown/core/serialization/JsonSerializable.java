package net.forthecrown.core.serialization;

import com.google.gson.JsonObject;

public interface JsonSerializable {
    JsonObject serialize();
}
