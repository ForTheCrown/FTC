package net.forthecrown.guilds.menu;

import net.forthecrown.guilds.Guild;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;

public class GuildMenus {

  public static final ContextSet SET = ContextSet.create();

  public static final ContextOption<Guild> GUILD = SET.newOption();
  public static final ContextOption<Integer> PAGE = SET.newOption(0);
  public static final ContextOption<Integer> DISC_PAGE = SET.newOption(0);

  public static final MainGuildMenu MAIN_MENU = new MainGuildMenu();

  public static final GuildDiscoveryMenu DISCOVERY_MENU
      = new GuildDiscoveryMenu();

  public static final MultiplierMenu MULTIPLIER_MENU
      = new MultiplierMenu(null);

  public static void open(MenuPage page, User viewer, Guild guild) {
    var context = SET.createContext();
    context.set(GUILD, guild);

    page.getMenu().open(viewer, context);
  }
}