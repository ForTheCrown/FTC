package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.concurrent.CompletableFuture;

public class TypeCreator {

    private static final DynamicCommandExceptionType EXCEPTION = new DynamicCommandExceptionType(name -> new LiteralMessage("Unknown rank: " + name));
    private static final DynamicCommandExceptionType EXCEPTION_BRANCH = new DynamicCommandExceptionType(name -> new LiteralMessage("Unknown branch: " + name));

    public static <S> CompletableFuture<Suggestions> listRankSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listRankSuggestions(context.getSource(), context, builder);
    }

    public static <S> CompletableFuture<Suggestions> listRankSuggestions(S source, CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getInput();
        String token = input.substring(input.lastIndexOf(' ')).trim();

        for (Rank r: Rank.values()){
            if(token.isBlank() || r.toString().regionMatches(true, 0, token, 0, token.length())) builder.suggest(r.toString());
        }
        return builder.buildFuture();
    }

    public static Rank getRank(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        try{
            return Rank.valueOf(c.getArgument(argument, String.class).toUpperCase());
        } catch (IllegalArgumentException e) { throw EXCEPTION.create(c.getArgument(argument, String.class)); }
    }

    public static <S> CompletableFuture<Suggestions> listBranchSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
        String input = builder.getInput();
        String token = input.substring(input.lastIndexOf(' ')).trim();

        for (Branch r: Branch.values()){
            if(token.isBlank() || r.toString().regionMatches(true, 0, token, 0, token.length())) builder.suggest(r.toString());
        }
        return builder.buildFuture();
    }

    public static Branch getBranch(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        try{
            return Branch.valueOf(c.getArgument(argument, String.class).toUpperCase());
        } catch (IllegalArgumentException e) { throw EXCEPTION_BRANCH.create(c.getArgument(argument, String.class)); }
    }
}
