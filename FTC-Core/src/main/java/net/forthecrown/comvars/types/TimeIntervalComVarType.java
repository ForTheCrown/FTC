package net.forthecrown.comvars.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class TimeIntervalComVarType implements ComVarType<Long> {
    public static final Key KEY = Keys.forthecrown("time_interval");

    TimeIntervalComVarType() {
        Registries.COMVAR_TYPES.register(KEY, this);
    }

    @Override
    public String asParsableString(Long value) {
        return new TimePrinter(value).printString() + " or " + value + "ms";
    }

    @Override
    public Long parse(StringReader reader) throws CommandSyntaxException {
        return TimeArgument.time().parse(reader);
    }

    @Override
    public JsonElement serialize(Long value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Long deserialize(JsonElement element) {
        return element.getAsLong();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return TimeArgument.time().listSuggestions(context, builder);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
