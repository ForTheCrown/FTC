package net.forthecrown.text.parse;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class LinkFunction extends TextFunction {

  static final Pattern URL_SCHEME_PATTERN
      = Pattern.compile("^[a-z][a-z0-9+\\-.]*:");

  static final Pattern URL_PATTERN
      = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

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
  public Component format(MatchResult result, TextContext context) {
    boolean clean = context.has(ChatParseFlag.CLEAN_LINKS);
    String clickUrl = result.group();

    return Component.text(result.group())
        .style(clean ? CLEAN_STYLE : UNDERLINED_STYLE)
        .clickEvent(url(clickUrl));
  }

  static ClickEvent url(String url) {
    if (!URL_SCHEME_PATTERN.matcher(url).find()) {
      url = "http://" + url;
    }

    return ClickEvent.openUrl(url);
  }
}