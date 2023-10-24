package net.forthecrown.leaderboards.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.arguments.SuggestionFunction;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.leaderboards.ScoreFilter;
import net.forthecrown.leaderboards.ScoreFilter.Operation;

public class FilterParser implements Suggester<CommandSource> {

  private final StringReader reader;

  private Suggester<CommandSource> suggester;

  public FilterParser(StringReader reader) {
    this.reader = reader;
  }

  public ScoreFilter parse() throws CommandSyntaxException {
    Operation op;
    int value;
    ScoreFilter and;

    suggest(reader.getCursor(), suggestOps());
    op = parseOp();
    reader.skipWhitespace();

    suggest(reader.getCursor(), suggestNumbers());
    value = reader.readInt();

    suggest(reader.getCursor(), suggestAnd());
    int beforeSkip = reader.getCursor();

    reader.skipWhitespace();

    if (reader.canRead() && reader.peek() == '&') {
      reader.skip();
      reader.skipWhitespace();
      and = parse();
    } else {
      and = null;
      reader.setCursor(beforeSkip);
    }

    return new ScoreFilter(value, op, and);
  }

  Operation parseOp() throws CommandSyntaxException {
    int peek = reader.peek();
    return switch (peek) {
      case '<' -> {
        reader.skip();

        if (reader.canRead() && reader.peek() == '=') {
          yield Operation.LESS_THAN_EQUAL;
        }

        yield Operation.LESS_THAN;
      }

      case '>' -> {
        reader.skip();

        if (reader.canRead() && reader.peek() == '=') {
          yield Operation.GREATER_THAN_EQUAL;
        }

        yield Operation.GREATER_THAN;
      }

      case '!' -> {
        reader.skip();
        reader.expect('=');
        yield Operation.NOT_EQUALS;
      }

      case '=' -> {
        reader.skip();
        yield Operation.EQUALS;
      }

      default -> {
        throw Grenadier.exceptions().createWithContext(
            "Expected one of '<', '>', '<=', '>=, '=' or '!='",
            reader
        );
      }
    };
  }

  private SuggestionFunction suggestOps() {
    return (builder, source) -> Completions.suggest(builder, "<", ">", "<=", ">=", "=", "!=");
  }

  private SuggestionFunction suggestNumbers() {
    return (builder, source) -> Completions.suggest(builder, "1", "5", "10", "100", "1000");
  }

  private SuggestionFunction suggestAnd() {
    return (builder, source) -> Completions.suggest(builder, "&");
  }

  private void suggest(int cursor, SuggestionFunction... functions) {
    this.suggester = (context, builder) -> {
      if (builder.getStart() != cursor) {
        builder = builder.createOffset(cursor);
      }

      for (SuggestionFunction function : functions) {
        function.suggest(builder, context.getSource());
      }

      return builder.buildFuture();
    };
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    if (suggester == null) {
      suggest(builder.getStart(), suggestOps());
    }

    return suggester.getSuggestions(context, builder);
  }
}
