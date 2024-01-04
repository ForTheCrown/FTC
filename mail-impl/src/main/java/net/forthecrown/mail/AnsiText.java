package net.forthecrown.mail;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import net.forthecrown.text.AbstractFlattenerListener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class AnsiText extends AbstractFlattenerListener {

  static final char ESCAPE_CHAR = '\033';

  private static final TextDecoration[] SUPPORTED_DECORATIONS = {
      TextDecoration.BOLD,
      TextDecoration.UNDERLINED,
      TextDecoration.ITALIC,
      TextDecoration.STRIKETHROUGH
  };

  private static final String[] DECORATION_CODES = {
      ESCAPE_CHAR + "[1m",
      ESCAPE_CHAR + "[4m",
      ESCAPE_CHAR + "[3m",
      ESCAPE_CHAR + "[9m",
  };

  private static final List<TextColor> SUPPORTED_COLORS = List.of(
      NamedTextColor.GRAY,
      NamedTextColor.DARK_GRAY,
      NamedTextColor.RED,
      NamedTextColor.DARK_RED,
      NamedTextColor.GREEN,
      NamedTextColor.YELLOW,
      NamedTextColor.BLUE,
      NamedTextColor.DARK_BLUE,
      NamedTextColor.LIGHT_PURPLE,
      NamedTextColor.DARK_PURPLE,
      NamedTextColor.AQUA,
      NamedTextColor.WHITE
  );

  private static final Int2IntMap COLOR_CODE_TABLE = createColorTable();

  static final String RESET = ESCAPE_CHAR + "[0m";
  static final String BOLD = DECORATION_CODES[0];
  static final String ELLIPSES = RESET + BOLD + "...";
  static final String SUFFIX = "\n(To reply to this message, join the server)";

  private final StringBuilder builder;
  private final int maxLength;
  private boolean lengthMaxedOut = false;

  public AnsiText(StringBuilder builder, int maxLength) {
    this.builder = builder;
    this.maxLength = maxLength - ELLIPSES.length();
  }

  private static Int2IntMap createColorTable() {
    Int2IntMap map = new Int2IntOpenHashMap();
    map.defaultReturnValue(-1);

    // Statements grouped by ANSI color code
    // Some colors aren't supported by ANSI, and all 24bit RGB codes aren't supported by Discord's
    // ANSI text, so we need to rely on the closest approximations basically

    map.put(NamedTextColor.GRAY.value(), 30);
    map.put(NamedTextColor.DARK_GRAY.value(), 30);

    map.put(NamedTextColor.RED.value(), 31);
    map.put(NamedTextColor.DARK_RED.value(), 31);

    map.put(NamedTextColor.GREEN.value(), 32);

    map.put(NamedTextColor.YELLOW.value(), 33);

    map.put(NamedTextColor.BLUE.value(), 34);
    map.put(NamedTextColor.DARK_BLUE.value(), 34);

    map.put(NamedTextColor.LIGHT_PURPLE.value(), 35);
    map.put(NamedTextColor.DARK_PURPLE.value(), 35);

    map.put(NamedTextColor.AQUA.value(), 36);

    map.put(NamedTextColor.WHITE.value(), 37);

    return Int2IntMaps.unmodifiable(map);
  }

  static String replaceControlCodes(String str) {
    return str.replace(ESCAPE_CHAR + "", "\\033");
  }

  @Override
  public void component(@NotNull String text) {
    append(text.replace("```", ""), true);
  }

  void append(String string, boolean allowTruncating) {
    if (lengthMaxedOut) {
      return;
    }

    int length = builder.length();
    int newLength = string.length() + length;

    if (newLength >= maxLength) {
      if (allowTruncating) {
        int remainingLength = maxLength - length;
        String truncated = string.substring(0, remainingLength);
        builder.append(truncated);
      }

      builder.append(ELLIPSES);
      builder.append(SUFFIX);

      lengthMaxedOut = true;

      return;
    }

    builder.append(string);
  }

  @Override
  protected void flattenStyle() {
    Style before = style;
    super.flattenStyle();
    Style after = style;

    TextColor colorBefore = before.color();
    TextColor colorAfter  = after.color();
    boolean colorChanged  = !Objects.equals(colorBefore, colorAfter);

    // Only underlined and bold are supported by Discord's ANSI, but who cares, maybe one day
    // they'll improve their ANSI renderer and actually let it support the whole ANSI thing
    DecoChange[] changes  = scanChanges(before, after);
    boolean requiresReset = false;

    for (DecoChange change : changes) {
      requiresReset |= change.requiresReset();
    }

    if (requiresReset) {
      append(RESET, false);
    }

    for (int i = 0; i < changes.length; i++) {
      DecoChange change = changes[i];

      if (change.after && !change.before) {
        append(DECORATION_CODES[i], false);
      }
    }

    if (colorChanged || requiresReset) {
      append(toAnsiColor(colorAfter), false);
    }
  }

  private static DecoChange[] scanChanges(Style before, Style after) {
    int supportedLen = SUPPORTED_DECORATIONS.length;
    DecoChange[] result = new DecoChange[supportedLen];

    for (int i = 0; i < supportedLen; i++) {
      TextDecoration deco = SUPPORTED_DECORATIONS[i];
      result[i] = new DecoChange(deco, before.hasDecoration(deco), after.hasDecoration(deco));
    }

    return result;
  }

  private record DecoChange(TextDecoration deco, boolean before, boolean after) {
    boolean requiresReset() {
      return before && !after;
    }
  }

  private static String toAnsiColor(@Nullable TextColor color) {
    if (color == null || color.value() == NamedTextColor.WHITE.value()) {
      // White lol
      return ESCAPE_CHAR + "[2;37m";
    }

    // RGB colors aren't supported by Discord's ANSI, so round to the closest supported color,
    // which may not be supported anyway ;-;
    color = TextColor.nearestColorTo(SUPPORTED_COLORS, color);

    int foundCode = COLOR_CODE_TABLE.get(color.value());
    if (foundCode != -1) {
      return ESCAPE_CHAR + "[2;" + foundCode + "m";
    }

    return String.format("%s[38;2;%s;%s;%sm", ESCAPE_CHAR, color.red(), color.green(),color.blue());
  }
}
