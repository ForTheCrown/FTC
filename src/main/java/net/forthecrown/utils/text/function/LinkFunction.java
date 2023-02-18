package net.forthecrown.utils.text.function;

import static net.forthecrown.utils.text.ChatParser.FLAG_CLEAN_LINKS;
import static net.forthecrown.utils.text.ChatParser.FLAG_LINKS;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.utils.text.TextFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

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
    super(FLAG_LINKS, URL_PATTERN.pattern());
  }

  @Override
  public @Nullable Component render(MatchResult group, int flags)
      throws CommandSyntaxException {
    boolean clean = (flags & FLAG_CLEAN_LINKS) != 0;
    String clickUrl = group.group();

    return Component.text(group.group())
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