package net.forthecrown.serializer;

import com.google.gson.JsonObject;

public interface JsonBufSerializable extends JsonSerializable {
    JsonBuf serializeAsBuf();

    @Override
    default JsonObject serialize() {
        return serializeAsBuf().getSource();
    }
}
