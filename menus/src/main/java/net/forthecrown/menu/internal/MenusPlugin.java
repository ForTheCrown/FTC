package net.forthecrown.menu.internal;

import net.forthecrown.events.Events;
import org.bukkit.plugin.java.JavaPlugin;

public class MenusPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Events.register(new MenuListener());
  }
}