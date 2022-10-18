package net.forthecrown.events.economy;

import com.sk89q.worldguard.protection.flags.StateFlag;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcFlags;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Vars;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.events.Events;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopCreateListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSignShopCreate(SignChangeEvent event1) {
        Events.runSafe(event1.getPlayer(), event1, event -> {
            if(Text.toString(event.line(0)).isBlank()) {
                return;
            }

            Player player = event.getPlayer();

            String line0 = Text.plain(event.line(0));
            String line1 = Text.plain(event.line(1));
            String line2 = Text.plain(event.line(2));
            String line3 = Text.plain(event.line(3));

            ShopType shopType;
            switch (line0.toLowerCase()) {
                default: return; //switch statement to set the shop's type

                case "-[buy]-":
                case "=[buy]=":
                case "(buy)":
                case "[buy]":
                case "<buy>":
                    if(player.hasPermission(Permissions.SHOP_ADMIN)
                            && player.getGameMode() == GameMode.CREATIVE
                    ) {
                        shopType = ShopType.ADMIN_BUY;
                    } else {
                        shopType = ShopType.BUY;
                    }

                    break;

                case "-[sell]-":
                case "=[sell]=":
                case "(sell)":
                case "[sell]":
                case "<sell>":
                    if(player.hasPermission(Permissions.SHOP_ADMIN)
                            && player.getGameMode() == GameMode.CREATIVE
                    ) {
                        shopType = ShopType.ADMIN_SELL;
                    } else {
                        shopType = ShopType.SELL;
                    }

                    break;
            }

            // No price given
            if (line3.isBlank()) {
                throw Exceptions.SHOP_NO_PRICE;
            }

            Sign sign = (Sign) event.getBlock().getState();
            String lastLine = line3.toLowerCase().replaceAll("[\\D]", "").trim();

            // Parse price
            int price;
            try {
                price = Integer.parseInt(lastLine);
            } catch (Exception e) {
                throw Exceptions.SHOP_NO_PRICE;
            }

            // Make sure they don't exceed the max shop price
            if (price > Vars.maxSignShopPrice) {
                throw Exceptions.shopMaxPrice();
            }

            // They must give at least one line of info about the shop
            if (line2.isBlank() && line1.isBlank()) {
                throw Exceptions.SHOP_NO_DESC;
            }

            //WorldGuard flag check
            StateFlag.State state = FtcFlags.query(player.getLocation(), FtcFlags.SHOP_CREATION);

            if(state == StateFlag.State.DENY
                    && !player.hasPermission(Permissions.ADMIN)
            ) {
                player.sendMessage(Messages.WG_CANNOT_MAKE_SHOP);
                return;
            }

            ShopManager shopManager = Crown.getEconomy().getShops();

            //creates the signshop
            SignShop shop = shopManager.createSignShop(sign.getLocation(), shopType, price, player.getUniqueId());

            //Opens the example item selection screen
            player.openInventory(SignShops.createExampleInventory());
            Events.register(new ExampleItemSelectionListener(player, shop));

            if(shopType == ShopType.BUY) {
                event.line(0, shopType.getUnStockedLabel());
            } else {
                event.line(0, shopType.getStockedLabel());
            }

            event.line(3, SignShops.priceLine(price));
        });
    }

    public class ExampleItemSelectionListener implements Listener{
        private final Player player;
        private final SignShop shop;

        public ExampleItemSelectionListener(Player p, SignShop s){
            player = p;
            shop = s;
        }

        @EventHandler
        public void invClickEvent(InventoryClickEvent event){
            if (event.getCurrentItem() == null
                    || !event.getWhoClicked().equals(player)
            ) {
                return;
            }

            if(event.getCurrentItem().getType() == Material.BARRIER) {
                event.setCancelled(true);
            }
        }

        //sets the example item and adds the item(s) to the shop's inventory
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if(!event.getPlayer().equals(player)
                    || event.getInventory().getType() != InventoryType.HOPPER
            ) {
                return;
            }

            Player player = (Player) event.getPlayer();

            Events.unregister(this);

            Inventory inv = event.getInventory();
            var shopInv = shop.getInventory();

            ItemStack item = inv.getContents()[SignShops.EXAMPLE_ITEM_SLOT];
            if(ItemStacks.isEmpty(item)){ //If example item was not found: destroy shop and tell them why they failed
                shop.destroy(false);
                player.sendMessage(Messages.SHOP_CREATE_FAILED);

                return;
            }

            //Add the item to the inventory
            shop.setExampleItem(item);
            shopInv.addItem(item.clone());

            //Send the info message
            player.sendMessage(Messages.createdShop(shop));

            shop.update();
            shop.delayUnload();
        }
    }
}