package net.forthecrown.antigrief.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;

public class ReasonParser implements ArgumentType<String> {

  @Override
  public String parse(StringReader reader) throws CommandSyntaxException {
    if (reader.peek() == '"' || reader.peek() == '\'') {
      return reader.readQuotedString();
    }

    var remaining = reader.getRemaining();
    reader.setCursor(reader.getTotalLength());

    return remaining;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder, "\"\"", "''");
  }
}
