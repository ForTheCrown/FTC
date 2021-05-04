package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.ShopManager;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.events.customevents.SignShopUseEvent;
import net.forthecrown.core.types.signs.SignManager;
import net.forthecrown.core.utils.Cooldown;
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
        if(Cooldown.contains(event.getPlayer())) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(ShopManager.isShop(event.getClickedBlock())){
            Cooldown.add(event.getPlayer(), 6);

            SignShop shop = ShopManager.getShop(event.getClickedBlock().getLocation());
            if(shop == null) return;

            Player player = event.getPlayer();

            //Can't use in spectator lol
            if(player.getGameMode() == GameMode.SPECTATOR) return;

            //checks if they're the owner and if they're sneaking, then opens the shop inventory to edit it
            if(player.isSneaking() && (shop.getOwner().equals(player.getUniqueId()) || player.hasPermission("ftc.admin"))){
                player.openInventory(shop.getInventory());
                FtcCore.getInstance().getServer().getPluginManager().registerEvents(new SignShopInteractSubClass(player, shop), FtcCore.getInstance());
                return;
            }

            //Call the event
            new SignShopUseEvent(shop, UserManager.getUser(player), player, FtcCore.getBalances()).callEvent();
        } else if(SignManager.isInteractableSign(event.getClickedBlock())){
            try {
                SignManager.getSign(event.getClickedBlock().getLocation()).interact(event.getPlayer());
            } catch (NullPointerException ignored) {}
        }
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