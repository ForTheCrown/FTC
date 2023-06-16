package net.forthecrown.text.parse;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.Loggers;
import net.kyori.adventure.text.Component;

public class HyperlinkFunction extends TextFunction {

  public HyperlinkFunction() {
    super(Pattern.compile("\\[[^\\]]+\\]\\((?:(https?):\\/\\/)?([-\\w_.]+\\.\\w{2,})(\\/\\S*)?\\)"));
  }

  @Override
  public boolean test(TextContext context) {
    return context.has(ChatParseFlag.CLEAN_LINKS);
  }

  @Override
  public Component format(MatchResult result, TextContext context) {
    String input = result.group();
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