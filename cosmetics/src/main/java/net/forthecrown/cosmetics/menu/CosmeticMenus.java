package net.forthecrown.cosmetics.menu;

import net.forthecrown.Loggers;
import net.forthecrown.user.User;
import org.slf4j.Logger;

public final class CosmeticMenus {
  private CosmeticMenus() {}

  static final Logger LOGGER = Loggers.getLogger();

  private static MainPage page;

  public static void open(User user) {
    page.getMenu().open(user);
  }

  public static void createMenus() {
    page = new MainPage();
  }
}