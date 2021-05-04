package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.grenadier.CommandSource;

import java.util.concurrent.CompletableFuture;

public class ComVarArgument implements ArgumentType<ComVar<?>> {
    public static final ComVarArgument COM_VAR = new ComVarArgument();

    public static final DynamicCommandExceptionType UNKNOWN_VAR = new DynamicCommandExceptionType(o -> () -> "Unkown variable: " + o);

    public static ComVarArgument comVar(){
        return COM_VAR;
    }

    public static ComVar<?> getComVar(CommandContext<CommandSource> c, String argument){
        return c.getArgument(argument, ComVar.class);
    }

    @Override
    public ComVar<?> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        ComVar<?> result = ComVars.getVar(name);
        if(result == null){
            reader.setCursor(cursor);
            throw UNKNOWN_VAR.createWithContext(reader, name);
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(builder, ComVars.getVariables());
    }
}
