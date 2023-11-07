package net.forthecrown.afk;

import net.forthecrown.user.User;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.user.name.NameElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class AfkNameElement implements NameElement {

  @Override
  public @Nullable Component createDisplay(User user, DisplayContext context) {
    if (!Afk.isAfk(user)) {
      return null;
    }
    if (!context.intentMatches(DisplayIntent.TABLIST)) {
      return null;
    }

    return Component.text(" [AFK]", NamedTextColor.GRAY);
  }
}
