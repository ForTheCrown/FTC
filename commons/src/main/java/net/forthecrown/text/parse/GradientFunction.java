package net.forthecrown.text.parse;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class GradientFunction extends TextFunction {

  public GradientFunction() {
    super(Pattern.compile("< *((?:crazy[ \\-_]*)?gradient) *= *([a-zA-Z0-9&#, ]+): *(.+) *>"));
  }

  @Override
  public boolean test(TextContext context) {
    return context.has(ChatParseFlag.GRADIENTS);
  }

  @Override
  public Component format(MatchResult result, TextContext context) {
    String label        = result.group(1);
    String colorsInput  = result.group(2);
    String text         = result.group(3);

    GradientColorSpace colorSpace;

    if (label.contains("crazy")) {
      colorSpace = GradientColorSpace.HSV;
    } else {
      colorSpace = GradientColorSpace.RGB;
    }

    TextColor[] colors = parseColors(colorsInput);

    if (colors == null) {
      return null;
    }

    return colorSpace.render(text, colors);
  }

  private static TextColor[] parseColors(String input) {
    String[] split = input.split("( ?)+,( ?)+");
    TextColor[] colors = new TextColor[split.length];

    for (int i = 0; i < split.length; i++) {
      String name = split[i];

      // Blank color name, fail
      if (name == null || name.isBlank()) {
        return null;
      }

      TextColor color = getColor(name);

      if (color == null) {
        return null;
      }

      colors[i] = color;
    }

    return colors;
  }

  private static TextColor getColor(String s) {
    if (s.startsWith("0x")) {
      // Replace color codes so next if statement
      // picks up that this is a hex color code
      s = "#" + s.substring(2);
    }

    if (s.startsWith("#")) {
      return TextColor.fromHexString(s);
    }

    // Not a hex code -> get by color name
    return NamedTextColor.NAMES.value(s);
  }

  public enum GradientColorSpace {
    RGB {
      @Override
      public Component render(String text, TextColor[] colors) {
        return Text.gradient(text, false, colors);
      }
    },

    HSV {
      @Override
      public Component render(String text, TextColor[] colors) {
        return Text.gradient(text, true, colors);
      }
    };

    public abstract Component render(String text, TextColor[] colors);
  }
}