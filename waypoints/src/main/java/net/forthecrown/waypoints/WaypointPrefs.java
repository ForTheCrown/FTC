package net.forthecrown.waypoints;

import java.util.UUID;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.forthecrown.waypoints.menu.WaypointOrder;

public class WaypointPrefs {

  public static final UserProperty<Boolean> INVITES_ALLOWED
      = Properties.booleanProperty("regionInvites", true);

  public static final UserProperty<Boolean> HULK_SMASH_ENABLED
      = Properties.booleanProperty("hulkSmash", true);

  public static final UserProperty<Boolean> HULK_SMASHING
      = Properties.booleanProperty("hulkSmashing", false);

  public static final UserProperty<Boolean> MENU_ORDER_INVERTED
      = Properties.booleanProperty("waypoints/menu_order_inverted", false);

  public static final UserProperty<WaypointOrder> MENU_ORDER
      = Properties.enumProperty("waypoints/list_order", WaypointOrder.NAME);

  public static final UserProperty<UUID> HOME_PROPERTY = Properties.uuidProperty()
      .key("homeWaypoint")
      .defaultValue(Waypoints.NIL_UUID)
      .build();

  static void createSettings(SettingsBook<User> settingsBook) {
    Setting hulkSmashing = Setting.create(WaypointPrefs.HULK_SMASH_ENABLED)
        .setDescription("Toggles hulk smashing poles")
        .setDisplayName("Hulk Smashing")
        .setToggle("N{1} hulk smashing poles")
        .setDisableDescription("Disable hulk smashing")
        .setEnableDescription("Enable hulk smashing")
        .createCommand(
            "hulksmash",
            WPermissions.WAYPOINTS,
            WPermissions.WAYPOINTS_ADMIN,
            "togglehulksmashing"
        );

    Setting regionInvites = Setting.create(WaypointPrefs.INVITES_ALLOWED)
        .setDescription("Allows/disables receiving and sending waypoint invites")
        .setDisplayName("Region Invites")
        .setToggle("N{1} accepting region invites")
        .createCommand(
            "regioninvites",
            WPermissions.WAYPOINTS,
            WPermissions.WAYPOINTS_ADMIN,
            "toggleregioninvites"
        );

    var list = settingsBook.getSettings();
    list.add(regionInvites.toBookSettng());
    list.add(hulkSmashing.toBookSettng());
  }
}
