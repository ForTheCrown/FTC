package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandHelpType implements ArgumentType<Integer> {
    public static final CommandHelpType HELP_TYPE = new CommandHelpType();
    private CommandHelpType() {}

    public static int MAX;

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        int page = reader.readInt();

        if(page > 0) page--;
        if(page > MAX) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(GrenadierUtils.correctReader(reader, cursor), page, MAX);

        return page;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < MAX; i++) suggestions.add("" + (i + 1));

        return CompletionProvider.suggestMatching(builder, suggestions);
    }
}
