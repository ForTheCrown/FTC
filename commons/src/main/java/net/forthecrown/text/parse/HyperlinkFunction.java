package net.forthecrown.text.parse;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;

public class HyperlinkFunction extends TextFunction {

  static final int GROUP_NAME = 1;
  static final int GROUP_URL = 2;

  public HyperlinkFunction() {
    super(createPattern());
  }

  private static Pattern createPattern() {
    String urlPattern = LinkFunction.URL_PATTERN.pattern();
    String prefix = "\\[([\\w\\s]+)\\]\\((";
    String suffix = ")\\)";

    return Pattern.compile(prefix + urlPattern + suffix);
  }

  @Override
  public boolean test(TextContext context) {
    return context.has(ChatParseFlag.CLEAN_LINKS);
  }

  @Override
  public Component format(MatchResult result, TextContext context) {
    String text = result.group(GROUP_NAME);
    String url  = result.group(GROUP_URL);

    return Component.text(text)
        .style(LinkFunction.UNDERLINED_STYLE)
        .hoverEvent(Component.text("Link: " + url))
        .clickEvent(Text.openUrl(url));
  }
}