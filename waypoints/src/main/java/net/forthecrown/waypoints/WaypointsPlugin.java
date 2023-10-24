package net.forthecrown.waypoints;

import net.forthecrown.FtcServer;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.packet.PacketListeners;
import net.forthecrown.packet.SignRenderer;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.User;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import net.forthecrown.waypoints.command.WaypointCommands;
import net.forthecrown.waypoints.listeners.WaypointsListeners;
import net.forthecrown.waypoints.type.WaypointTypes;
import org.bukkit.plugin.java.JavaPlugin;

public class WaypointsPlugin extends JavaPlugin {

  public WaypointConfig wConfig;

  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    reloadConfig();

    WaypointTypes.registerAll();
    WaypointManager.instance = new WaypointManager(this);

    SettingsBook<User> settingsBook = FtcServer.server().getGlobalSettingsBook();
    WaypointPrefs.createSettings(settingsBook);

    saver = PeriodicalSaver.create(
        WaypointManager.getInstance()::save,
        () -> wConfig.autoSaveInterval
    );

    saver.start();

    WaypointCommands.createCommands(WaypointManager.instance);
    WaypointsListeners.registerAll();

    Registry<SignRenderer> renderers = PacketListeners.listeners().getSignRenderers();
    renderers.register("waypoint_edit_sign", new WaypointSignRenderer(WaypointManager.instance));
  }

  @Override
  public void onLoad() {
    WaypointWorldGuard.registerAll();
  }

  @Override
  public void onDisable() {
    saver.stop();

    var m = WaypointManager.getInstance();
    m.clear();

    Registry<SignRenderer> renderers = PacketListeners.listeners().getSignRenderers();
    renderers.remove("waypoint_edit_sign");
  }

  @Override
  public void reloadConfig() {
    wConfig = TomlConfigs.loadPluginConfig(this, WaypointConfig.class);
  }
}