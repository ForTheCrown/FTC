package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DynamicOps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
abstract class PrimitiveSerializerParser<T> implements SerializerParser<T> {
    @Getter
    private final ArgumentType<T> argumentType;

    @Override
    public @NotNull String asString(@NotNull T value) {
        return value.toString();
    }

    @Override
    public <V> @NotNull V serialize(@NotNull DynamicOps<V> ops, @NotNull T value) {
        if (value instanceof Number number) {
            return ops.createNumeric(number);
        }

        if (value instanceof Boolean b) {
            return ops.createBoolean(b);
        }

        if (value instanceof String s) {
            return ops.createString(s);
        }

        return ops.empty();
    }
}