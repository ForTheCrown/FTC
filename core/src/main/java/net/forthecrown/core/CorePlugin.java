package net.forthecrown.core;

import net.forthecrown.core.grave.GraveImpl;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    GraveImpl.init();
  }
}