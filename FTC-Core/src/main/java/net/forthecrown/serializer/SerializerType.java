package net.forthecrown.serializer;

import com.google.gson.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public interface SerializerType<T>
        extends Keyed,
        JsonDeserializer<T>, JsonSerializer<T>
{
    T deserialize(JsonElement element);
    JsonElement serialize(T value);

    @Override
    default T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }

    @Override
    default JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src);
    }

    @Override
    @NotNull Key key();
}
