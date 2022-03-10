package net.forthecrown.vars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.vars.ParseFunction;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class PrimitiveVarType<T> implements VarType<T> {
    private final ParseFunction<T> fromString;
    private final Function<T, JsonElement> serializationFunc;
    private final Function<JsonElement, T> deserializationFunc;
    private final SuggestionProvider<CommandSource> suggestionProvider;
    private final Key key;

    PrimitiveVarType(Class<T> clazz, ParseFunction<T> fromString, Function<T, JsonElement> json, Function<JsonElement, T> func) {
        this(clazz, fromString, json, func, (c, b) -> Suggestions.empty());
    }

    PrimitiveVarType(Class<T> clazz, ParseFunction<T> fromString, Function<T, JsonElement> json, Function<JsonElement, T> func, SuggestionProvider<CommandSource> suggestionProvider) {
        this.fromString = fromString;
        this.suggestionProvider = suggestionProvider;
        this.serializationFunc = json;
        deserializationFunc = func;
        key = Keys.forthecrown(clazz.getSimpleName().toLowerCase() + "_type");

        Registries.VAR_TYPES.register(key, this);
    }

    @Override
    public T parse(StringReader input) throws CommandSyntaxException {
        return fromString.parse(input);
    }

    @Override
    public String asParsableString(T value) {
        return value == null ? "null" : value.toString();
    }

    @Override
    public JsonElement serialize(@Nullable T value) {
        return value == null ? JsonNull.INSTANCE : serializationFunc.apply(value);
    }

    @Override
    public T deserialize(JsonElement element) {
        return deserializationFunc.apply(element);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> c, SuggestionsBuilder b) throws CommandSyntaxException {
        return suggestionProvider.getSuggestions(c, b);
    }

    static SuggestionProvider<CommandSource> fromType(ArgumentType<?> type) {
        return type::listSuggestions;
    }
}
