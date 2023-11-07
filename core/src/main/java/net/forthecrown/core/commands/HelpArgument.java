package net.forthecrown.core.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Readers;

public class HelpArgument implements ArgumentType<String> {

  @Override
  public String parse(StringReader reader) throws CommandSyntaxException {
    char peeked = reader.peek();

    if (StringReader.isQuotedStringStart(peeked)) {
      return reader.readQuotedString();
    }

    return Readers.readUntilWhitespace(reader);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource source)) {
      return builder.buildFuture();
    }

    return FtcHelpList.helpList().suggest(source, builder);
  }
}
