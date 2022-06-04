package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Parses a page number for /baltop
 */
public class BaltopArgument implements ArgumentType<Integer> {
    private IntegerArgumentType intArg;

    public static BaltopArgument BALTOP = new BaltopArgument();
    private BaltopArgument() {
        intArg = IntegerArgumentType.integer(1, MAX);
    }

    //The max page number
    public static int MAX = Math.round(((float) Crown.getEconomy().getMap().size())/10);

    public static void resetMax() {
        MAX = Math.round(((float) Crown.getEconomy().getMap().size())/10);
        BALTOP.intArg = IntegerArgumentType.integer(1, MAX);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        resetMax();

        int cursor = reader.getCursor();
        int read = intArg.parse(reader);

        if(read > 0) read--;
        if(read > MAX) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(GrenadierUtils.correctReader(reader, cursor), read, MAX);

        return read;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < MAX; i++) suggestions.add("" + (i + 1));

        return CompletionProvider.suggestMatching(builder, suggestions);
    }

    public IntegerArgumentType getHandle() {
        return intArg;
    }
}