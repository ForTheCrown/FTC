package net.forthecrown.commands.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.VarRegistry;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;

import java.util.concurrent.CompletableFuture;

public class GlobalVarArgument implements ArgumentType<Var<?>>, VanillaMappedArgument {
    public static final GlobalVarArgument COM_VAR = new GlobalVarArgument();

    public static final DynamicCommandExceptionType UNKNOWN_VAR = new DynamicCommandExceptionType(o -> () -> "Unkown variable: '" + o + "'");

    public static GlobalVarArgument comVar(){
        return COM_VAR;
    }

    public static Var<?> getComVar(CommandContext<CommandSource> c, String argument){
        return c.getArgument(argument, Var.class);
    }

    @Override
    public Var<?> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        try {
            VarRegistry.validateName(name);
        } catch (IllegalArgumentException e) {
            throw FtcExceptionProvider.create(e.getMessage());
        }

        Var<?> result = VarRegistry.getVar(name);
        if(result == null) throw UNKNOWN_VAR.createWithContext(GrenadierUtils.correctReader(reader, cursor), name);

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String token = builder.getRemainingLowerCase();

        for (Var v: VarRegistry.getValues()) {
            if(!v.getName().toLowerCase().contains(token)) continue;

            Message tooltip = GrenadierUtils.componentToMessage(v.asComponent());
            builder.suggest(v.getName(), tooltip);
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.word();
    }
}