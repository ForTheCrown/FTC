package net.forthecrown.utils.text.function;

import static net.forthecrown.utils.text.ChatParser.FLAG_PLAYER_TAGGING;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.MatchResult;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.TextFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class PlayerFunction extends TextFunction {

  public PlayerFunction() {
    super(FLAG_PLAYER_TAGGING, "@[a-zA-Z0-9_-]+");
  }

  @Override
  public @Nullable Component render(MatchResult group, int flags)
      throws CommandSyntaxException {
    // Skip the first '@' in the input
    String name = group.group().substring(1);

    UserLookup lookup = UserManager.get().getUserLookup();
    UserLookupEntry entry = lookup.get(name);

    if (entry == null) {
      return null;
    }

    var user = Users.get(entry);
    Component displayName = user.displayName();
    user.unloadIfOffline();

    return text()
        .append(text("@"))
        .append(displayName)
        .hoverEvent(displayName.hoverEvent())
        .clickEvent(displayName.clickEvent())
        .color(NamedTextColor.AQUA)
        .build();
  }
}