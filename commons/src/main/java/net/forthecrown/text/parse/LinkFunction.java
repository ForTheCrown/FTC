package net.forthecrown.text.parse;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class LinkFunction extends TextFunction {

  static final Pattern URL_PATTERN
      = Pattern.compile("(?:\\$\\{)*(?:(?:https?)://)?(?:[-\\w_.-]+\\.(?:[a-zA-Z_$]){2,63})(?::[0-9]+)?(?:/\\S*)?}?");

  static final Style CLEAN_STYLE = Style.empty();
  static final Style UNDERLINED_STYLE = Style.style()
      .color(NamedTextColor.AQUA)
      .decorate(TextDecoration.UNDERLINED)
      .build();

  public LinkFunction() {
    super(URL_PATTERN);
  }

  @Override
  public boolean test(TextContext context) {
    return context.has(ChatParseFlag.LINKS);
  }

  @Override
  public boolean filter(MatchResult result, TextContext context) {
    return !result.group().startsWith("${");
  }

  @Override
  public Component format(MatchResult result, TextContext context) {
    if (result.group().startsWith("${")) {
      return null;
    }

    boolean clean = context.has(ChatParseFlag.CLEAN_LINKS);
    String clickUrl = result.group();

    return Component.text(result.group())
        .style(clean ? CLEAN_STYLE : UNDERLINED_STYLE)
        .clickEvent(Text.openUrl(clickUrl));
  }
}