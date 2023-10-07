package net.forthecrown.webmap;

import net.forthecrown.BukkitServices;
import net.forthecrown.FtcServer;
import net.forthecrown.events.Events;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.webmap.bluemap.BlueWebmap;
import net.forthecrown.webmap.dynmap.DynmapWebmap;
import net.forthecrown.webmap.listeners.GameModeListener;
import org.bukkit.plugin.java.JavaPlugin;

public class WebmapPlugin extends JavaPlugin {

  private WebMap implementation;

  @Override
  public void onEnable() {
    boolean dynmap = PluginUtil.isEnabled("dynmap");
    boolean blumap = PluginUtil.isEnabled("BlueMap");

    if (blumap && dynmap) {
      getSLF4JLogger().warn(
          "Both BlueMap and Dynmap found, only BlueMap will be kept in sync "
              + "plugin-induced map changes"
      );
    }

    if (blumap) {
      implementation = new BlueWebmap(getDataFolder().toPath());
    } else if (dynmap) {
      implementation = new DynmapWebmap();
    } else {
      getSLF4JLogger().error("No Dynmap or BlueMap plugin found... disabling self");

      var pl = getServer().getPluginManager();
      pl.disablePlugin(this);

      return;
    }

    BukkitServices.register(WebMap.class, implementation);
    HideSetting.createSetting(FtcServer.server().getGlobalSettingsBook());
    Events.register(new GameModeListener());
  }

  @Override
  public void onDisable() {
    if (implementation instanceof BlueWebmap webmap) {
      webmap.save();
    }
  }
}
