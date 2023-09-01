package net.forthecrown.king;

import net.forthecrown.titles.UserRanks;
import net.forthecrown.user.User;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.NameElement;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class MonarchNameElement implements NameElement {

  static final String NAME = "monarch_title";

  private final Kingship kingship;

  public MonarchNameElement(Kingship kingship) {
    this.kingship = kingship;
  }

  @Override
  public @Nullable Component createDisplay(User user, DisplayContext context) {
    if (!UserRanks.showRank(context)) {
      return null;
    }

    if (!kingship.isMonarch(user.getUniqueId())) {
      return null;
    }

    return kingship.getPrefix();
  }
}
