package net.forthecrown.waypoints;

import net.forthecrown.FtcServer;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.User;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import net.forthecrown.waypoints.command.WaypointCommands;
import net.forthecrown.waypoints.listeners.WaypointsListeners;
import org.bukkit.plugin.java.JavaPlugin;

public class WaypointsPlugin extends JavaPlugin {

  public WaypointConfig wConfig;

  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    reloadConfig();
    WaypointManager.instance = new WaypointManager(this);

    createSettings();

    saver = PeriodicalSaver.create(
        WaypointManager.getInstance()::save,
        () -> wConfig.autoSaveInterval
    );

    saver.start();

    WaypointCommands.createCommands();
    WaypointsListeners.registerAll();
  }

  @Override
  public void onDisable() {
    saver.stop();

    var m = WaypointManager.getInstance();
    m.save();
    m.clear();
  }

  @Override
  public void reloadConfig() {
    wConfig = TomlConfigs.loadPluginConfig(this, WaypointConfig.class);
  }

  private void createSettings() {
    Setting hulkSmashing = Setting.create(Waypoints.HULK_SMASH_ENABLED)
        .setDescription("Toggles hulk smashing poles")
        .setDisplayName("Hulk Smashing")
        .setToggleMessage("N{1} hulk smashing poles")
        .setDisableDescription("Disable hulk smashing")
        .setEnableDescription("Enable hulk smashing")
        .createCommand(
            "hulksmash",
            WPermissions.WAYPOINTS,
            WPermissions.WAYPOINTS_ADMIN,
            "togglehulksmashing"
        );

    Setting regionInvites = Setting.create(Waypoints.INVITES_ALLOWED)
        .setDescription("Allows/disables receiving and sending waypoint invites")
        .setDisplayName("Region Invites")
        .setToggleMessage("N{1} accepting region invites")
        .createCommand(
            "regioninvites",
            WPermissions.WAYPOINTS,
            WPermissions.WAYPOINTS_ADMIN,
            "toggleregioninvites"
        );

    SettingsBook<User> settingsBook = FtcServer.server().getGlobalSettingsBook();
    var list = settingsBook.getSettings();
    list.add(regionInvites.toBookSettng());
    list.add(hulkSmashing.toBookSettng());
  }
}