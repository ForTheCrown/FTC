package net.forthecrown.mail.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.mail.MailPermissions;

public class MailOptionsArgument implements ArgumentType<EnumSet<MailSendOption>> {

  static final MailSendOption[] OPTIONS = MailSendOption.values();

  @Override
  public EnumSet<MailSendOption> parse(StringReader reader) throws CommandSyntaxException {
    if (reader.peek() == '-') {
      throw Exceptions.formatWithContext("Expected '-' to start options ", reader);
    }

    EnumSet<MailSendOption> options = EnumSet.noneOf(MailSendOption.class);

    outer: while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
      char ch = reader.peek();

      for (MailSendOption option : OPTIONS) {
        if (option.getCharacter() != ch) {
          continue;
        }

        if (options.contains(option)) {
          throw Exceptions.formatWithContext("Flag '{0}' already set", reader, ch);
        }

        reader.skip();
        options.add(option);

        continue outer;
      }

      throw Exceptions.formatWithContext("Unknown flag '{0}'", reader, ch);
    }

    return options;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource source)) {
      return Suggestions.empty();
    }

    if (!source.hasPermission(MailPermissions.MAIL_FLAGS)) {
      return Suggestions.empty();
    }

    String input = builder.getRemainingLowerCase();

    if (!input.startsWith("-")) {
      builder.suggest("-");
      return builder.buildFuture();
    }

    for (MailSendOption option : OPTIONS) {
      if (input.indexOf(option.getCharacter()) != -1) {
        continue;
      }

      builder.suggest(input + option.getCharacter());
    }

    return builder.buildFuture();
  }
}
