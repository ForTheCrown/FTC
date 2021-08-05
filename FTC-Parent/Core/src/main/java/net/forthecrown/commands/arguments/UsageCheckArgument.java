package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.useables.preconditions.UsageCheck;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public class UsageCheckArgument implements ArgumentType<Key> {
    private static final UsageCheckArgument INSTANCE = new UsageCheckArgument();
    private UsageCheckArgument() {}

    public static final DynamicCommandExceptionType UNKNOWN_PRECONDITION = new DynamicCommandExceptionType(o -> () -> "Unknown precondition: " + o);

    public static UsageCheckArgument precondition(){
        return INSTANCE;
    }

    public static UsageCheck<?> getCheck(CommandContext<CommandSource> c, String argument){
        return Registries.USAGE_CHECKS.get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = CoreCommands.ftcKeyType().parse(reader);

        try {
            Registries.USAGE_CHECKS.get(key).key();
        } catch (NullPointerException e){
            throw UNKNOWN_PRECONDITION.createWithContext(GrenadierUtils.correctReader(reader, cursor), key.asString());
        }

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return FtcSuggestionProvider.suggestRegistry(builder, Registries.USAGE_CHECKS);
    }
}
