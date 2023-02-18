package net.forthecrown.utils.text.function;

import static net.forthecrown.utils.text.ChatParser.FLAG_CLEAN_LINKS;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.MatchResult;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.text.TextFunction;
import net.kyori.adventure.text.Component;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;

public class HyperlinkFunction extends TextFunction {
  public HyperlinkFunction() {
    super(FLAG_CLEAN_LINKS, makePattern());
  }

  private static @RegExp String makePattern() {
    @RegExp
    String pattern = "\\[[^\\]]+\\]\\((?:(https?):\\/\\/)?([-\\w_.]+\\.\\w{2,})(\\/\\S*)?\\)";
    return pattern;
  }

  @Override
  public @Nullable Component render(MatchResult group, int flags)
      throws CommandSyntaxException {
    String input = group.group();
    Loggers.getLogger().debug("input={}", input);

    input = input.substring(1, input.length() - 1);

    int textCloseIndex = input.indexOf(']');

    if (textCloseIndex == -1) {
      return null;
    }

    String text = input.substring(0, textCloseIndex);
    String url = input.substring(textCloseIndex + 2);

    return Component.text(text)
        .style(LinkFunction.UNDERLINED_STYLE)
        .hoverEvent(Component.text("Link: " + url))
        .clickEvent(LinkFunction.url(url));
  }
}