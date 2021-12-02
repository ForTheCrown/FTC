package net.forthecrown.commands.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;

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

        try {
            ComVarRegistry.validateName(name);
        } catch (IllegalArgumentException e) {
            throw FtcExceptionProvider.create(e.getMessage());
        }

        ComVar<?> result = ComVarRegistry.getVar(name);
        if(result == null) throw UNKNOWN_VAR.createWithContext(GrenadierUtils.correctReader(reader, cursor), name);

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String token = builder.getRemainingLowerCase();

        for (ComVar v: ComVarRegistry.getValues()) {
            if(!v.getName().toLowerCase().startsWith(token)) continue;

            Message tooltip = GrenadierUtils.componentToMessage(v.prettyDisplay());
            builder.suggest(v.getName(), tooltip);
        }

        return builder.buildFuture();
    }
}
