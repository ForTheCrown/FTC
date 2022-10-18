package net.forthecrown.events.economy;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ShopDestroyListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSignDestroy(BlockBreakEvent event) {
        if (!SignShops.isShop(event.getBlock())) {
            return;
        }

        SignShop shop = Crown.getEconomy()
                .getShops()
                .getShop(event.getBlock());

        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(shop.getOwner())
                && !player.hasPermission(Permissions.SHOP_ADMIN)
        ) {
            player.sendMessage(Messages.SHOP_CANNOT_DESTROY);
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