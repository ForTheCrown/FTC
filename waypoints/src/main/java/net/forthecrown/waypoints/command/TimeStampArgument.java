package net.forthecrown.waypoints.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.SuggestionFunction;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.grenadier.types.ArgumentTypes;

public class TimeStampArgument implements ArgumentType<Instant> {

  @Override
  public Instant parse(StringReader reader) throws CommandSyntaxException {
    Parser<?> parser = new Parser<>(reader);
    return parser.parse();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    StringReader reader = Readers.forSuggestions(builder);
    Parser<S> parser = new Parser<>(reader);

    try {
      parser.parse();
    } catch (CommandSyntaxException exc) {
      // Ignored :)
    }

    return parser.getSuggestions(context, builder);
  }

  private class Parser<S> implements Suggester<S> {

    private final StringReader reader;
    private Suggester<S> suggester;

    public Parser(StringReader reader) {
      this.reader = reader;
    }

    private void suggestLabels() {
      suggester = (context, builder) -> {
        return Completions.suggest(builder,
            "iso:",
            "now",
            "now-10m",
            "now+10m",
            String.valueOf(System.currentTimeMillis()),
            "iso:" + Instant.now().toString()
        );
      };
    }

    private void suggest(int off, SuggestionFunction function) {
      suggester = (context, builder) -> {
        if (off != builder.getStart()) {
          builder = builder.createOffset(off);
        }

        function.suggest(builder, (CommandSource) context.getSource());
        return builder.buildFuture();
      };
    }

    private void suggestOffset() {
      int off = reader.getCursor();
      suggester = (context, builder) -> {
        builder = builder.createOffset(off);
        return ArgumentTypes.time().listSuggestions(context, builder);
      };
    }

    private Duration parseOffset() throws CommandSyntaxException {
      int start = reader.getCursor();
      suggest(start, (builder, source) -> {
        Completions.suggest(builder, "+", "-");
      });

      if (!reader.canRead() || Character.isWhitespace(reader.peek())) {
        return null;
      }

      int prefixChar = reader.peek();

      if (prefixChar == '-' || prefixChar == '+') {
        reader.skip();
        suggestOffset();
      } else {
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .dispatcherExpectedArgumentSeparator()
            .createWithContext(reader);
      }

      Duration dur = ArgumentTypes.time().parse(reader);

      if (prefixChar == '-') {
        dur = dur.multipliedBy(-1);
      }

      return dur;
    }

    public Instant parse() throws CommandSyntaxException {
      suggestLabels();

      if (Readers.startsWithIgnoreCase(reader, "now")) {
        reader.expect('n');
        reader.expect('o');
        reader.expect('w');

        Instant date = Instant.now();
        Duration offset = parseOffset();

        if (offset != null) {
          date = date.plus(offset);
        }

        return date;
      }

      if (Readers.startsWithIgnoreCase(reader, "iso:")) {
        reader.readUnquotedString();
        reader.expect(':');
        reader.skipWhitespace();

        int start = reader.getCursor();

        suggester = (context, builder) -> {
          builder = builder.createOffset(start);
          return Completions.suggest(builder, Instant.now().toString());
        };

        String end = Readers.readUntilWhitespace(reader);

        try {
          return Instant.parse(end);
        } catch (DateTimeParseException exc) {
          reader.setCursor(start);
          throw Exceptions.formatWithContext("Invalid date '{0}'", reader, end);
        }
      }

      long l = reader.readLong();
      return Instant.ofEpochMilli(l);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<S> context,
        SuggestionsBuilder builder
    ) {
      if (suggester == null) {
        suggestLabels();
      }

      return suggester.getSuggestions(context, builder);
    }
  }
}
