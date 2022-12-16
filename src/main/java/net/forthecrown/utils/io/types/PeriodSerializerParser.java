package net.forthecrown.utils.io.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class PeriodSerializerParser implements SerializerParser<Long> {
    @Override
    public @NotNull String asString(@NotNull Long value) {
        return value.toString();
    }

    @Override
    public @NotNull Component display(@NotNull Long value) {
        return Text.format("{0, time} or {0, number}ms", value);
    }

    @Override
    public <V> @NotNull V serialize(@NotNull DynamicOps<V> ops, @NotNull Long value) {
        return SerializerParsers.LONG.serialize(ops, value);
    }

    @Override
    public <V> @NotNull DataResult<Long> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
        return SerializerParsers.LONG.deserialize(ops, element);
    }

    @Override
    public @NotNull ArgumentType<Long> getArgumentType() {
        return TimeArgument.time();
    }
}