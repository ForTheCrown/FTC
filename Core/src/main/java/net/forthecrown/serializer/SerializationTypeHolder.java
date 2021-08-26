package net.forthecrown.serializer;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface SerializationTypeHolder<T> {
    SerializerType<? extends T> serializerType();

    default SerializerType<T> normalSerializerType() {
        return (SerializerType<T>) serializerType();
    }

    default @NotNull Key serializerKey() {
        return serializerType().key();
    }
}
