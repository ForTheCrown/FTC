package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.useables.preconditions.UsageCheck;
import net.forthecrown.utils.SuggestionUtils;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public class CheckArgType implements ArgumentType<Key> {
    private static final CheckArgType INSTANCE = new CheckArgType();
    private CheckArgType() {}

    public static final DynamicCommandExceptionType UNKNOWN_PRECONDITION = new DynamicCommandExceptionType(o -> () -> "Unknown precondition: " + o);

    public static CheckArgType precondition(){
        return INSTANCE;
    }

    public static UsageCheck<?> getCheck(CommandContext<CommandSource> c, String argument){
        return Registries.USAGE_CHECKS.get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = KeyType.ftc().parse(reader);

        try {
            Registries.USAGE_CHECKS.get(key).key();
        } catch (NullPointerException e){
            throw UNKNOWN_PRECONDITION.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), key.asString());
        }

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SuggestionUtils.suggestRegistry(builder, Registries.USAGE_CHECKS);
    }
}
