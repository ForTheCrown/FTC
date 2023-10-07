package net.forthecrown.vanilla;

import net.forthecrown.packet.PacketListeners;
import net.forthecrown.vanilla.listeners.InjectionListeners;
import net.forthecrown.vanilla.packet.ListenersImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class VanillaPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    var listeners = ListenersImpl.getListeners();
    listeners.initalize();

    var services = Bukkit.getServicesManager();
    services.register(PacketListeners.class, listeners, this, ServicePriority.Normal);

    var pl = Bukkit.getPluginManager();
    pl.registerEvents(new InjectionListeners(listeners), this);

    DefaultRenderers.registerAll(listeners);
  }

  @Override
  public void onDisable() {
    ListenersImpl.getListeners().shutdown();
  }
}
