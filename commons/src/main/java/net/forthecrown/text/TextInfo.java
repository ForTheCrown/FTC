package net.forthecrown.text;

import javax.annotation.Nonnegative;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.map.MinecraftFont;
import org.jetbrains.annotations.NotNull;

public final class TextInfo {

  private static final MinecraftFont FONT = MinecraftFont.Font;

  public static int getCharPxWidth(char c) {
    var sprite = FONT.getChar(c);

    if (sprite != null) {
      return sprite.getWidth();
    }

    return switch (c) {
      case '!', '\'', ',', '.', ':', ';', 'i', '|' -> 1;
      case '`', 'l' -> 2;
      case '"', '(', ')', '*', 'I', '[', ']', 't', '{', '}', ' ' -> 3;
      case '<', '>', 'f', 'k' -> 4;
      case '@', '~', '✔' -> 6;
      case '✖' -> 7;
      default -> 5;
    };
  }

  public static int getPxWidth(String s) {
    return s.chars().reduce(0, (val, c) -> val + getCharPxWidth((char) c) + 1);
  }

  public static int length(Component c) {
    String s = Text.plain(c);
    return getPxWidth(s);
  }

  public static @NotNull String getFiller(@Nonnegative int pixels) {
    if (pixels < 2) {
      // I spent a good 30 minutes looking for a 1px wide character in
      // minecraft's texture sheets, god bless unicode
      // Name: 'saurashtra danda'
      return "\uA8CE";
    }

    if (pixels == 2) {
      return ".";
    }

    if (pixels == 3) {
      return "`";
    }

    StringBuilder buffer = new StringBuilder();

    // Test if pixel count is odd by testing
    // the first bit of the pixel integer
    if ((pixels & 1) == 0) {
      pixels -= 3;
      buffer.append("`");
    }

    int divided = pixels / (getCharPxWidth('.') + 1);
    buffer.append(".".repeat(divided));

    return buffer.toString();
  }
}