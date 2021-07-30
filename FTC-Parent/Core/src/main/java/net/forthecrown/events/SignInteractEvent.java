package net.forthecrown.events;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.shops.ShopInventory;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.events.custom.SignShopUseEvent;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Cooldown;
import org.bukkit.Bukkit;
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

public class SignInteractEvent implements Listener {

    @EventHandler
    public void onSignShopUser(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!ShopManager.isShop(event.getClickedBlock())) return;

        if(Cooldown.contains(event.getPlayer())) return;
        Cooldown.add(event.getPlayer(), 6);

        SignShop shop = ForTheCrown.getShopManager().getShop(event.getClickedBlock().getLocation());
        if(shop == null) return;

        Player player = event.getPlayer();

        //Can't use in spectator lol
        if(player.getGameMode() == GameMode.SPECTATOR) return;

        //checks if they're the owner and if they're sneaking, then opens the shop inventory to edit it
        if(player.isSneaking() && (shop.getOwner().equals(player.getUniqueId()) || player.hasPermission(Permissions.CORE_ADMIN))){
            player.openInventory(shop.getInventory());
            Bukkit.getPluginManager().registerEvents(new SignShopInteractSubClass(player, shop), ForTheCrown.inst());
            return;
        }

        //Call the event
        new SignShopUseEvent(shop, UserManager.getUser(player), player, ForTheCrown.getBalances()).callEvent();
    }

    public class SignShopInteractSubClass implements Listener {

        private final SignShop shop;
        private final Player player;

        public SignShopInteractSubClass(Player player, SignShop shop){
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

            shopInv.clear();

            for (ItemStack item : contents){
                if(item == null) continue;

                if(!item.getType().equals(example.getType())){
                    player.getInventory().addItem(item);
                    continue;
                }

                shopInv.addItem(item);
            }
            shopInv.checkStock();
        }
    }
}