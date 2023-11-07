package net.forthecrown.leaderboards.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.leaderboards.ScoreFilter;

public class FilterArgument implements ArgumentType<ScoreFilter> {

  @Override
  public ScoreFilter parse(StringReader reader) throws CommandSyntaxException {
    FilterParser parser = new FilterParser(reader);
    return parser.parse();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    StringReader reader = Readers.forSuggestions(builder);
    FilterParser parser = new FilterParser(reader);

    try {
      parser.parse();
    } catch (CommandSyntaxException exc) {
      // Ignored
    }

    return parser.getSuggestions((CommandContext<CommandSource>) context, builder);
  }
}
