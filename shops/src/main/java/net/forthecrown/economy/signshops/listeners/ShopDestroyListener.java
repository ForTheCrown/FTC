package net.forthecrown.economy.signshops.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.signshops.ShopManager;
import net.forthecrown.economy.signshops.SignShop;
import net.forthecrown.economy.signshops.SignShops;
import net.forthecrown.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ShopDestroyListener implements Listener {

  private final ShopManager manager;

  public ShopDestroyListener(ShopManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onSignDestroy(BlockBreakEvent event) {
    if (!SignShops.isShop(event.getBlock())) {
      return;
    }

    SignShop shop = manager.getShop(event.getBlock());
    Player player = event.getPlayer();

    event.setCancelled(true);

    if (!player.getUniqueId().equals(shop.getOwner())
        && !player.hasPermission(EconPermissions.SHOP_ADMIN)
    ) {
      player.sendMessage(EconMessages.SHOP_CANNOT_DESTROY);
      return;
    }

    //This shit dumb
    //Destroy the block 1 tick later, because some dumb shit event cancelled it so we gotta do it again.
    Tasks.runLater(() -> shop.destroy(true), 1);
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockDestroy(BlockDestroyEvent event) {
    //Don't allow the block to be broken if it's a shop
    if (SignShops.isShop(event.getBlock())) {
      event.setCancelled(true);
    }
  }

}