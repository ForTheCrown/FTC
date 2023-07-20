package net.forthecrown.waypoints;

import java.util.Objects;
import java.util.UUID;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;

public class WaypointPrefs {

  public static final UserProperty<Boolean> INVITES_ALLOWED = Properties.booleanProperty()
      .key("regionInvites")
      .defaultValue(true)
      .build();

  public static final UserProperty<Boolean> HULK_SMASH_ENABLED = Properties.booleanProperty()
      .key("hulkSmash")
      .defaultValue(true)
      .build();

  public static final UserProperty<Boolean> HULK_SMASHING = Properties.booleanProperty()
      .key("hulkSmash_active")
      .defaultValue(false)
      .build();

  public static final UserProperty<UUID> HOME_PROPERTY = Properties.uuidProperty()
      .key("homeWaypoint")
      .defaultValue(Waypoints.NIL_UUID)
      .callback((user, value, oldValue) -> {
        var manager = WaypointManager.getInstance();

        if (!Objects.equals(oldValue, Waypoints.NIL_UUID)) {
          Waypoint old = manager.get(oldValue);
          old.removeResident(user.getUniqueId());
        }

        if (!Objects.equals(value, Waypoints.NIL_UUID)) {
          Waypoint newHome = manager.get(value);
          newHome.addResident(user.getUniqueId());
        }
      })
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
