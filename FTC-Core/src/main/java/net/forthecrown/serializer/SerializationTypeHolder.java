package net.forthecrown.serializer;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface SerializationTypeHolder<T> {
    SerializerType<? extends T> getType();

    default SerializerType<T> normalType() {
        return (SerializerType<T>) getType();
    }

    default @NotNull Key typeKey() {
        return getType().key();
    }
}
