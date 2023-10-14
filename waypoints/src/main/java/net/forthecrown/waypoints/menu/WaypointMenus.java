package net.forthecrown.waypoints.menu;

import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.waypoints.Waypoint;

public final class WaypointMenus {
  private WaypointMenus() {}

  public static final ContextSet SET = ContextSet.create();
  public static final ContextOption<Integer> PAGE = SET.newOption(0);
  public static final ContextOption<Waypoint> WAYPOINT = SET.newOption();

  public static final WaypointListPage LIST_PAGE;
  public static final EditMenu EDIT_MENU;
  public static final WaypointListPage NO_PERMS;
  public static final ResidentsList RESIDENTS_LIST;

  static {
    LIST_PAGE = new WaypointListPage(null);
    EDIT_MENU = new EditMenu();
    RESIDENTS_LIST = new ResidentsList(null);
    NO_PERMS  = new WaypointListPage(RESIDENTS_LIST);
  }

  public static void open(MenuPage page, User user, Waypoint waypoint) {
    Context context = SET.createContext();

    if (waypoint != null) {
      context.set(WAYPOINT, waypoint);
    }

    page.getMenu().open(user, context);
  }
}
