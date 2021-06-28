package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public class ActionArgType implements ArgumentType<Key> {
    private static final ActionArgType INSTANCE = new ActionArgType();
    private ActionArgType() {}

    public static DynamicCommandExceptionType UNKNOWN_ACTION = new DynamicCommandExceptionType(o -> () -> "Unknown action: " + o);

    public static ActionArgType action(){
        return INSTANCE;
    }

    public static UsageAction getAction(CommandContext<CommandSource> c, String argument){
        return CrownCore.getActionRegistry().getAction(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = CrownUtils.parseKey(reader);

        try {
            CrownCore.getActionRegistry().getAction(key);
        } catch (NullPointerException e){
            throw UNKNOWN_ACTION.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), key.asString());
        }

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CrownUtils.suggestKeys(builder, CrownCore.getActionRegistry().getKeys());
    }
}
