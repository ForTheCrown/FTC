package net.forthecrown.utils.text;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import net.forthecrown.utils.text.function.EmoteFunction;
import net.forthecrown.utils.text.function.GradientFunction;
import net.forthecrown.utils.text.function.GradientFunction.GradientColorSpace;
import net.forthecrown.utils.text.function.HyperlinkFunction;
import net.forthecrown.utils.text.function.LinkFunction;
import net.forthecrown.utils.text.function.PlayerFunction;
import net.forthecrown.utils.text.function.TimeFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public class TextFunctions {
  private TextFunctions() {}

  private static final List<TextFunction> functions = new ObjectArrayList<>();

  static {
    addAll();
  }

  private static void addAll() {
    add(new GradientFunction("gradient", GradientColorSpace.RGB));
    add(new GradientFunction("crazy gradient", GradientColorSpace.HSV));
    add(new PlayerFunction());
    add(new TimeFunction());
    add(new HyperlinkFunction());
    add(new LinkFunction());
    add(new EmoteFunction());
  }

  public static Component render(Component text, int flags) {
    for (var e: functions) {
      if ((flags & e.getFlags()) != e.getFlags()) {
        continue;
      }

      TextReplacementConfig config = TextReplacementConfig.builder()
          .match("\\\\?" + e.getPattern())

          .replacement((result, builder) -> {
            if (result.group().startsWith("\\")) {
              return Component.text(result.group().substring(1));
            }

            try {
              Component resultText = e.render(result, flags);

              return Objects.requireNonNullElseGet(
                  resultText,
                  () -> text(result.group())
              );
            } catch (CommandSyntaxException exc) {
              return text(result.group());
            }
          })

          .build();

      text = text.replaceText(config);
    }

    return text;
  }


  public static void add(TextFunction function) {
    functions.add(function);
  }
}