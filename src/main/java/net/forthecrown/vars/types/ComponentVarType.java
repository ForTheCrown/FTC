package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import net.forthecrown.commands.arguments.Arguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * A class for the Component com var type
 */
public class ComponentVarType implements VarType<Component> {
    @Override
    public @NotNull String asString(@NotNull Component value) {
        return GsonComponentSerializer.gson().serialize(value);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Component value) {
        return GsonComponentSerializer.gson().serializeToTree(value);
    }

    @Override
    public @NotNull Component deserialize(@NotNull JsonElement element) {
        return GsonComponentSerializer.gson().deserializeFromTree(element);
    }

    @Override
    public @NotNull Component display(@NotNull Component value) {
        return value;
    }

    @Override
    public @NotNull ArgumentType<Component> getArgumentType() {
        return Arguments.CHAT;
    }
}