package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface VarType<T> {

    @NotNull String asString(@NotNull T value);

    default @NotNull Component display(@NotNull T value) {
        return Component.text(asString(value));
    }

    @NotNull
    ArgumentType<T> getArgumentType();

    @NotNull
    JsonElement serialize(@NotNull T value);

    @NotNull
    T deserialize(@NotNull JsonElement element);
}