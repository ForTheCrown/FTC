package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.ShopInventory;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Cooldown;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopInteractionListener implements Listener {

    @EventHandler
    public void onSignShopUser(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!ShopManager.isShop(event.getClickedBlock())) return;
        if(Cooldown.containsOrAdd(event.getPlayer(), 6)) return;

        ShopManager manager = Crown.getShopManager();
        SignShop shop = manager.getShop(event.getClickedBlock().getLocation());
        if(shop == null) return;

        Player player = event.getPlayer();

        //Can't use in spectator lol
        if(player.getGameMode() == GameMode.SPECTATOR) return;

        //Handle interaction between player and shop
        manager.getInteractionHandler().handleInteraction(shop, UserManager.getUser(player), Crown.getEconomy());
    }

    public static class ShopRestockListener implements Listener {

        private final SignShop shop;
        private final Player player;

        public ShopRestockListener(Player player, SignShop shop){
            this.player = player;
            this.shop = shop;
        }

        @EventHandler
        public void onInvClose(InventoryCloseEvent event) { //items added
            if(!event.getPlayer().equals(player)) return;

            Inventory inv = event.getInventory();
            ShopInventory shopInv = shop.getInventory();
            ItemStack[] contents = inv.getContents().clone();
            final ItemStack example = shopInv.getExampleItem();

            HandlerList.unregisterAll(this);

            //Clear the shop inventory because the contents are stored above
            shopInv.clear();

            //For every item, if the item can be added to the shop add it, otherwise
            //Give it back to the owner
            for (ItemStack item : contents){
                if(item == null) continue;

                if(!item.getType().equals(example.getType())){
                    player.getInventory().addItem(item);
                    continue;
                }

                shopInv.addItem(item);
            }

            shop.updateLastUse();
            shop.update();
        }
    }
}