package net.forthecrown.serializer;

import com.google.gson.JsonObject;

public interface JsonWrapperSerializable extends JsonSerializable {
    JsonWrapper serializeAsWrapper();

    @Override
    default JsonObject serialize() {
        return serializeAsWrapper().getSource();
    }
}
