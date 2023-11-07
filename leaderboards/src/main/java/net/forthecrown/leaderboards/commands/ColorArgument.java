package net.forthecrown.leaderboards.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.chat.FormatSuggestions;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

public class ColorArgument implements ArgumentType<Color> {

  static final int HEX_LENGTH = 8;

  @Override
  public Color parse(StringReader reader) throws CommandSyntaxException {
    if (reader.peek() == '#') {
      reader.skip();
      return parseHex(reader);
    } else if (Readers.startsWithIgnoreCase(reader, "0x")) {
      reader.skip();
      reader.skip();
      return parseHex(reader);
    }

    if (StringReader.isAllowedNumber(reader.peek())) {
      int value = reader.readInt();
      return Color.fromRGB(value);
    }

    int start = reader.getCursor();
    String name = reader.readUnquotedString();
    TextColor color = NamedTextColor.NAMES.value(name);

    if (color == null) {
      reader.setCursor(start);
      throw Exceptions.formatWithContext("Unknown color name '{0}'", reader, name);
    }

    return Color.fromRGB(color.red(), color.green(), color.blue());
  }

  static boolean isHexChar(int ch) {
    return (ch >= '0' && ch <= '9')
        || (ch >= 'a' && ch <= 'f')
        || (ch >= 'A' && ch <= 'F');
  }

  private Color parseHex(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();
    int readChars = 0;

    while (reader.canRead()) {
      if (isHexChar(reader.peek())) {
        if (readChars >= HEX_LENGTH) {
          throw Exceptions.formatWithContext("Hex sequence too long (Max 6 characters)", reader);
        }

        reader.skip();
        readChars++;
        continue;
      }

      if (readChars < HEX_LENGTH) {
        throw Exceptions.create("Hex color sequence too short (6 hex characters required)");
      }
    }

    int end = reader.getCursor();
    String hexSequence = reader.getString().substring(start, end);

    int argb = Integer.parseUnsignedInt(hexSequence, 16);
    return Color.fromARGB(argb);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    FormatSuggestions.HEX_2_NAME.forEach((string, string2) -> {
      if (!Completions.matches(builder.getRemainingLowerCase(), string)) {
        return;
      }

      builder.suggest(string, new LiteralMessage(string2));
    });

    Completions.suggest(builder, NamedTextColor.NAMES.keys());
    return builder.buildFuture();
  }
}
