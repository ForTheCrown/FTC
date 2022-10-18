package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.ArgumentType;
import net.forthecrown.text.Text;
import net.forthecrown.grenadier.types.TimeArgument;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class TimeIntervalVarType implements VarType<Long> {
    @Override
    public @NotNull String asString(@NotNull Long value) {
        return value.toString();
    }

    @Override
    public @NotNull Component display(@NotNull Long value) {
        return Text.format("{0, time} or {0, number}ms", value);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Long value) {
        return new JsonPrimitive(value);
    }

    @Override
    public @NotNull Long deserialize(@NotNull JsonElement element) {
        return element.getAsLong();
    }

    @Override
    public @NotNull ArgumentType<Long> getArgumentType() {
        return TimeArgument.time();
    }
}