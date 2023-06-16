package net.forthecrown.command.arguments.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.command.FtcSuggestions;
import net.forthecrown.command.arguments.SuggestionFunction;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.text.ChatEmotes;
import net.forthecrown.text.parse.ChatParseFlag;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.slf4j.Logger;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class MessageSuggestions {

  private static final Logger LOGGER = Loggers.getLogger();
  private static final String GRADIENT_START = "<gradient";

  private final CommandSender sender;
  private final StringReader reader;
  private final boolean override;

  private final SuggestionFunction customSuggestions;

  private Suggester<CommandSource> suggests;
  private boolean parsed;

  public void parse() {
    if (parsed) {
      return;
    }

    suggest(
        reader.getCursor(),
        this::suggestEmotes,
        this::suggestPlayers,
        customSuggestions
    );

    while (reader.canRead()) {
      if (peekMatches('<', ChatParseFlag.GRADIENTS.getPermission())) {
        suggestGradient();
        continue;
      }

      if (peekMatches('&', ChatParseFlag.COLORS.getPermission())) {
        suggestColorCode();
        continue;
      }

      if (peekMatches(':', ChatParseFlag.EMOJIS.getPermission())) {
        suggestEmote();
        continue;
      }

      if (reader.peek() == ' ') {
        suggest(
            reader.getCursor() + 1,
            this::suggestPlayers,
            this::suggestEmotes,
            customSuggestions
        );
      }

      reader.skip();
    }

    parsed = true;
  }

  private boolean peekMatches(char token, Permission permission) {
    return reader.peek() == token && (override || sender.hasPermission(permission));
  }

  private void suggestEmote() {
    final int start = reader.getCursor();

    suggest(start, this::suggestEmotes);

    // Skip first ':'
    reader.skip();

    // Read until next ':'
    while (reader.canRead()) {
      char peek = reader.peek();
      reader.skip();

      if (peek == ':') {
        return;
      }
    }
  }

  private void suggestColorCode() {
    final int start = reader.getCursor();
    suggest(start, this::suggestColorCodes);

    // Skip & char
    reader.skip();

    if (reader.canRead()) {
      // If hex code
      if (reader.peek() == '#') {
        suggestHex(start, "&#");
        return;
      }

      // the color code if it's there
      reader.skip();
    }
  }

  private void suggestHex(final int start, String prefix) {
    suggest(start, (builder, source) -> {
      FtcSuggestions.suggest(builder, FormatSuggestions.HEX_2_NAME);
    });

    // Skip the 6 hex digits, if possible
    int remaining = 6 + prefix.length();

    while (reader.canRead() && remaining > 0) {
      reader.skip();
      remaining--;
    }
  }

  private void suggestGradient() {
    suggest(reader.getCursor(), (builder, source) -> {
      Completions.suggest(builder, GRADIENT_START);
    });

    int charIndex = 0;
    while (reader.canRead()
        && charIndex < GRADIENT_START.length()
        && GRADIENT_START.charAt(charIndex++) == reader.peek()
    ) {
      reader.skip();
    }

    if (charIndex < GRADIENT_START.length()) {
      return;
    }

    if (!suggestColorLabel('=', ',')) {
      return;
    }

    if (!suggestColorLabel(',', ':')) {
      return;
    }

    reader.skipWhitespace();
    if (reader.canRead() && reader.peek() == ':') {
      reader.skip();
    }

    reader.skipWhitespace();

    if (!reader.canRead()) {
      suggest(
          reader.getCursor(),
          this::suggestPlayers,
          (builder, source) -> builder.suggest(">"),
          customSuggestions
      );

      return;
    }

    while (reader.canRead()) {
      if (reader.peek() == '>') {
        reader.skip();
        return;
      }

      if (reader.peek() != ' ') {
        reader.skip();
        continue;
      }

      suggest(
          reader.getCursor() + 1,
          this::suggestPlayers,
          (builder, source) -> builder.suggest(">"),
          customSuggestions
      );
      reader.skip();
    }
  }

  private boolean suggestColorLabel(char prefix, char suffix) {
    int cursor = reader.getCursor();

    reader.skipWhitespace();
    if (!reader.canRead()) {
      suggest(cursor, (builder, source) -> builder.suggest(prefix + ""));
      return false;
    }

    if (reader.peek() == prefix) {
      reader.skip();
      reader.skipWhitespace();
      cursor = reader.getCursor();
    } else {
      suggest(cursor, (builder, source) -> builder.suggest(prefix + ""));
      return false;
    }

    suggest(cursor, this::suggestColorNames);

    if (!reader.canRead()) {
      return false;
    }

    if (Readers.startsWith(reader, "0x")) {
      suggestHex(cursor, "0x");
    } else if (reader.peek() == '#') {
      suggestHex(cursor, "#");
    } else {
      while (reader.canRead() && reader.peek() != suffix) {
        reader.skip();
      }
    }

    reader.skipWhitespace();
    return reader.canRead();
  }

  private void suggestColorNames(SuggestionsBuilder builder, CommandSource source) {
    Completions.suggest(builder, FormatSuggestions.HEX_2_NAME.values());
  }

  private void suggestEmotes(SuggestionsBuilder builder, CommandSource source) {
    if (!source.hasPermission(ChatParseFlag.EMOJIS.getPermission()) && !override) {
      return;
    }

    ChatEmotes.addSuggestions(builder);
  }

  private void suggestColorCodes(SuggestionsBuilder builder, CommandSource source) {
    if (!source.hasPermission(ChatParseFlag.COLORS.getPermission()) && !override) {
      return;
    }

    FtcSuggestions.suggest(builder, FormatSuggestions.FORMAT_SUGGESTIONS);
  }

  private void suggestPlayers(SuggestionsBuilder builder, CommandSource source) {
    FtcSuggestions.suggestPlayerNames(source, builder, false);
  }

  private void suggest(int cursor, SuggestionFunction... suggestions) {
    this.suggests = (context, builder) -> {
      if (cursor != builder.getStart()) {
        builder = builder.createOffset(cursor);
      }

      for (var s : suggestions) {
        if (s == null) {
          continue;
        }

        s.suggest(builder, context.getSource());
      }

      return builder.buildFuture();
    };
  }

  public CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandSource> c,
                                                        SuggestionsBuilder builder
  ) {
    if (!parsed) {
      parse();
    }

    if (this.suggests == null) {
      builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

      var source = c.getSource();
      suggestPlayers(builder, source);
      suggestEmotes(builder, source);

      return builder.buildFuture();
    }

    return suggests.getSuggestions(c, builder);
  }

  public static <S> CompletableFuture<Suggestions> get(CommandContext<S> c,
                                                       SuggestionsBuilder builder
  ) {
    return get(c, builder, false);
  }

  public static <S> CompletableFuture<Suggestions> get(CommandContext<S> c,
                                                       SuggestionsBuilder builder, boolean override
  ) {
    return get(c, builder, override, null);
  }

  @SuppressWarnings("unchecked")
  public static <S> CompletableFuture<Suggestions> get(CommandContext<S> c,
                                                       SuggestionsBuilder builder,
                                                       boolean override,
                                                       SuggestionFunction customSuggestions
  ) {
    var suggester = of(
        ((CommandSource) c.getSource()).asBukkit(),
        at(builder.getInput(), builder.getStart()),
        override,
        customSuggestions
    );

    return suggester.listSuggestions((CommandContext<CommandSource>) c, builder);
  }

  private static StringReader at(String str, int cursor) {
    StringReader reader = new StringReader(str);
    reader.setCursor(cursor);

    return reader;
  }
}