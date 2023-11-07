package net.forthecrown.text.parse;

import static net.kyori.adventure.text.Component.text;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerFunction extends TextFunction {

  public PlayerFunction() {
    super(Pattern.compile("@[a-zA-Z0-9_-]+"));
  }

  @Override
  public boolean test(TextContext context) {
    return context.has(ChatParseFlag.TAGGING);
  }

  @Override
  public Component format(MatchResult result, TextContext context) {
    // Skip the first '@' in the input
    String name = result.group().substring(1);

    UserLookup lookup = Users.getService().getLookup();
    var entry = lookup.query(name);

    if (entry == null) {
      return null;
    }

    User user = Users.get(entry);
    Component displayName = user.displayName(context.viewer());

    return text()
        .append(text("@"))
        .append(displayName)
        .hoverEvent(displayName.hoverEvent())
        .clickEvent(displayName.clickEvent())
        .insertion(displayName.insertion())
        .color(NamedTextColor.AQUA)
        .build();
  }
}