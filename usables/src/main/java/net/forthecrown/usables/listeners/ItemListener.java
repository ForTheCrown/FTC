package net.forthecrown.usables.listeners;

import net.forthecrown.usables.Usables;
import net.forthecrown.usables.objects.UsableItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();
    var held = event.getItem();

    if (!Usables.isUsable(held)) {
      return;
    }

    UsableItem item = Usables.item(held);
    item.load();

    UsablesListeners.executeInteract(item, player, event);
  }
}
