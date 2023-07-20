package net.forthecrown.events;

import net.forthecrown.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class Events {
  private Events() {}

  /**
   * Registers the given listener
   *
   * @param listener The listener to register
   */
  public static void register(Listener listener) {
    Plugin caller = PluginUtil.currentContextPlugin();
    Bukkit.getPluginManager().registerEvents(listener, caller);
  }

  /**
   * Unregisters the given listener
   *
   * @param listener The listener to unregister
   */
  public static void unregister(Listener listener) {
    HandlerList.unregisterAll(listener);
  }
}