package net.forthecrown.core.commands;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.grenadier.CommandSource;

public class HelpArgument implements ArgumentType<String> {

  @Override
  public String parse(StringReader reader) throws CommandSyntaxException {
    if (!reader.canRead()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .dispatcherExpectedArgumentSeparator()
          .createWithContext(reader);
    }

    char peeked = reader.peek();

    if (StringReader.isQuotedStringStart(peeked)) {
      return reader.readQuotedString();
    }

    var str = readTopicName(reader);

    if (Strings.isNullOrEmpty(str)) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerExpectedStartOfQuote()
          .createWithContext(reader);
    }

    return str;
  }

  private String readTopicName(StringReader reader) {
    int start = reader.getCursor();

    while (reader.canRead()) {
      int ch = reader.peek();
      if (Character.isWhitespace(ch) || ch == ',') {
        break;
      }
      reader.skip();
    }

    return reader.getString().substring(start, reader.getCursor());
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
