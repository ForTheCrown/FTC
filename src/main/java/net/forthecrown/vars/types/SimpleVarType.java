package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.ArgumentType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@RequiredArgsConstructor
class SimpleVarType<T> implements VarType<T> {
    @Getter
    private final ArgumentType<T> argumentType;
    private final Function<JsonElement, T> deserializationFunc;

    @Override
    public @NotNull String asString(@NotNull T value) {
        return value.toString();
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull T value) {
        if (value instanceof Number number) {
            return new JsonPrimitive(number);
        }

        if (value instanceof String str) {
            return new JsonPrimitive(str);
        }

        return new JsonPrimitive((Boolean) value);
    }

    @Override
    public @NotNull T deserialize(@NotNull JsonElement element) {
        return deserializationFunc.apply(element);
    }
}