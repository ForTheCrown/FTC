package net.forthecrown.serializer;

import com.google.gson.JsonElement;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface SerializerType<T> extends Keyed {
    T deserialize(JsonElement element);
    JsonElement serialize(T value);

    @Override
    @NotNull Key key();
}
