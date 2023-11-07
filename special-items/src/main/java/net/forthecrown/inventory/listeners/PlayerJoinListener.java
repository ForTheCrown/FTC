package net.forthecrown.inventory.listeners;

import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.user.event.UserJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJoinListener implements Listener {


  @EventHandler(ignoreCancelled = true)
  public void onUserJoin(UserJoinEvent event) {
    if (!event.isFirstJoin()) {
      return;
    }

    var item = ExtendedItems.ROYAL_SWORD.createItem(event.getUser().getUniqueId());
    event.getPlayer().getInventory().addItem(item);
  }
}
