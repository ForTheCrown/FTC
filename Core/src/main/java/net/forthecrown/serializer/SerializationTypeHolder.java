package net.forthecrown.serializer;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface SerializationTypeHolder<T> {
    SerializerType<? extends T> getType();

    default SerializerType<T> normalSerializerType() {
        return (SerializerType<T>) getType();
    }

    default @NotNull Key serializerKey() {
        return getType().key();
    }
}
