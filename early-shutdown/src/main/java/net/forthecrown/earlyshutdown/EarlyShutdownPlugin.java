package net.forthecrown.earlyshutdown;

import net.forthecrown.events.EarlyShutdownEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EarlyShutdownPlugin extends JavaPlugin {

  @Override
  public void onEnable() {

  }

  @Override
  public void onDisable() {
    EarlyShutdownEvent event = new EarlyShutdownEvent();
    event.callEvent();
  }
}
