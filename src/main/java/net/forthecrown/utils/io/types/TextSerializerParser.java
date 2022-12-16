package net.forthecrown.utils.io.types;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.forthecrown.commands.arguments.Arguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * A class for the Component com var type
 */
public class TextSerializerParser implements SerializerParser<Component> {
    @Override
    public @NotNull String asString(@NotNull Component value) {
        return GsonComponentSerializer.gson().serialize(value);
    }

    @Override
    public <V> @NotNull V serialize(@NotNull DynamicOps<V> ops, @NotNull Component value) {
        if (ops instanceof JsonOps) {
            return (V) serialize(value);
        }

        return JsonOps.INSTANCE.convertTo(ops, serialize(value));
    }

    @Override
    public <V> @NotNull DataResult<Component> deserialize(@NotNull DynamicOps<V> ops, @NotNull V element) {
        if (ops instanceof JsonOps) {
            return DataResult.success(deserialize((JsonElement) element));
        }

        return DataResult.success(deserialize(ops.convertTo(JsonOps.INSTANCE, element)));
    }

    public @NotNull JsonElement serialize(@NotNull Component value) {
        return GsonComponentSerializer.gson().serializeToTree(value);
    }

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