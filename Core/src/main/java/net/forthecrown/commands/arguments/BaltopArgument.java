package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
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
    public static BaltopArgument BALTOP = new BaltopArgument();
    private BaltopArgument() {}

    //The max page number
    public static int MAX = Math.round(((float) Crown.getBalances().getMap().size())/10);

    public static void resetMax() {
        MAX = Math.round(((float) Crown.getBalances().getMap().size())/10);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        int read = reader.readInt();

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
}