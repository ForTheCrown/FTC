package net.forthecrown.events.economy;

import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopInventoryListener implements Listener {
    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof SignShop shop)) {
            return;
        }

        var player = event.getPlayer();

        Inventory inv = event.getInventory();
        Inventory shopInv = shop.getInventory();
        ItemStack[] contents = inv.getContents().clone();

        final ItemStack example = shop.getExampleItem();

        //Clear the shop inventory because the contents are stored above
        shopInv.clear();

        //For every item, if the item can be added to the shop add it, otherwise
        //Give it back to the owner
        for (ItemStack item : contents) {
            if (ItemStacks.isEmpty(item)) {
                continue;
            }

            if (!example.isSimilar(item)) {
                player.getInventory().addItem(item);
                continue;
            }

            shopInv.addItem(item);
        }

        shop.update();
    }
}