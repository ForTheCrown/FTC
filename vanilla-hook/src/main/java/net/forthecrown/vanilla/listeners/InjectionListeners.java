package net.forthecrown.vanilla.listeners;

import net.forthecrown.vanilla.packet.ListenersImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InjectionListeners implements Listener {

  final ListenersImpl listeners;

  public InjectionListeners(ListenersImpl listeners) {
    this.listeners = listeners;
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    listeners.inject(event.getPlayer());
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent event) {
    listeners.uninject(event.getPlayer());
  }
}
