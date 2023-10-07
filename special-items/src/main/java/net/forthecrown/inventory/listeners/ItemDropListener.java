package net.forthecrown.inventory.listeners;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.inventory.ExtendedItems;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {

  TextComponent CANNOT_DROP_SPECIAL = text("Cannot drop special item!", NamedTextColor.RED);

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (!ExtendedItems.isSpecial(event.getItemDrop().getItemStack())) {
      return;
    }

    event.getPlayer().sendActionBar(CANNOT_DROP_SPECIAL);
    event.setCancelled(true);
  }
}
