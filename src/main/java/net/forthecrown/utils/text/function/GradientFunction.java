package net.forthecrown.utils.text.function;

import static net.forthecrown.utils.text.ChatParser.FLAG_GRADIENTS;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

public class GradientFunction extends SquareBracketFunction {
  private final GradientColorSpace colorSpace;

  public GradientFunction(String label, GradientColorSpace colorSpace) {
    super(FLAG_GRADIENTS, label);
    this.colorSpace = colorSpace;
  }

  @Override
  protected @Nullable Component render(String input, int flags)
      throws CommandSyntaxException
  {
    int paramsEnd = input.indexOf(':');

    // I don't think this could happen because a
    // regex pattern is being used, but still
    if (paramsEnd == -1) {
      return null;
    }

    TextColor[] colors = parseColors(input.substring(0, paramsEnd));

    if (colors == null) {
      return null;
    }

    return colorSpace.render(input.substring(paramsEnd + 1), colors);
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