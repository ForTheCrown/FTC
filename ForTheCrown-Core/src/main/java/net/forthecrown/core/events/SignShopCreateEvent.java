package net.forthecrown.core.events;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.exceptions.CrownException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SignShopCreateEvent implements Listener {

    //WHAT AM I DOING
    @EventHandler(ignoreCancelled = true)
    public void onSignShopCreate(SignChangeEvent event){
        if(event.getLine(0).isBlank()) return;

        Player player = event.getPlayer();

        ShopType shopType;

        switch (event.getLine(0).toLowerCase()){
            default: return; //switch statement to set the shop's type, basically that's it

            case "-[buy]-":
            case "=[buy]=":
            case "(buy)":
            case "[buy]":
                if(player.hasPermission("ftc.signshop.admincreate") && player.getGameMode() == GameMode.CREATIVE) shopType = ShopType.ADMIN_BUY_SHOP;
                else shopType = ShopType.BUY_SHOP;
                break;

            case "-[sell]-":
            case "=[sell]=":
            case "(sell)":
            case "[sell]":
                if(player.hasPermission("ftc.signshop.admincreate") && player.getGameMode() == GameMode.CREATIVE) shopType = ShopType.ADMIN_SELL_SHOP;
                else shopType = ShopType.SELL_SHOP;
                break;
        }

        if(event.getLine(3).isBlank()) throw new CrownException(player, "&7The last line must contain a price!");

        Sign sign = (Sign) event.getBlock().getState();
        String lastLine = event.getLine(3).toLowerCase();

        lastLine = lastLine.replaceAll("[\\D]", ""); //replaces all letter chars, leaves only numbers to make the price
        lastLine = lastLine.replaceAll(" ", ""); //removes all spaces

        int price;
        try {
            price = Integer.parseInt(lastLine);
        } catch (Exception e){ throw new CrownException(player, "&7The last line must contain numbers!"); }

        if(event.getLine(2).isBlank() && event.getLine(1).isBlank()) throw new CrownException(player, "&7You must provide a description of the shop's items");

        SignShop shop = FtcCore.createSignShop(sign.getLocation(), shopType, price, player.getUniqueId()); //creates the signshop file

        player.openInventory(shop.getExampleInventory());

        FtcCore.getInstance().getServer().getPluginManager().registerEvents(new SignShopSubClass1(player, shop), FtcCore.getInstance());

        if(shopType == ShopType.BUY_SHOP) event.setLine(0, shopType.getOutOfStockLabel());
        else event.setLine(0, shopType.getInStockLabel());

        event.setLine(3, ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + price); //idk, I thought putting ALL_CODES would make triggering events involving the shops harder
    }

    public class SignShopSubClass1 implements Listener{

        private final Player player;
        private final SignShop shop;

        public SignShopSubClass1(Player p, SignShop s){
            player = p;
            shop = s;
        }

        @EventHandler
        public void invClickEvent(InventoryClickEvent event){
            if(event.getCurrentItem() == null) return;
            if(!event.getWhoClicked().equals(player)) return;
            if(event.getCurrentItem().getType() == Material.BARRIER) event.setCancelled(true);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event){ //sets the example item and adds the item(s) to the shop's inventory
            if(!event.getPlayer().equals(player)) return;
            if(event.getInventory().getType() != InventoryType.HOPPER) return;

            Player player = (Player) event.getPlayer();
            Sign sign = (Sign) shop.getBlock().getState();

            HandlerList.unregisterAll(this);

            Inventory inv = event.getInventory();
            ShopInventory shopInv = shop.getInventory();

            ItemStack item = inv.getContents()[2];
            if(item == null){
                sign.setLine(3, ChatColor.DARK_GRAY + "Price " + ChatColor.RESET + shop.getPrice());
                sign.update();
                shop.destroyShop();
                throw new CrownException(player, "&4Shop creation failed! &cNo item in the inventory");
            }

            shopInv.setExampleItem(item);
            shopInv.addItem(item);

            String loooooonngg = ChatColor.GREEN + "SignShop created!" + ChatColor.RESET + " It'll " +
                    "sell " +
                    shopInv.getExampleItem().getAmount() + " " +
                    CrownUtils.getItemNormalName(shopInv.getExampleItem()) +
                    " for " + shop.getPrice() +
                    " Rhines.";

            if(shop.getType() == ShopType.SELL_SHOP || shop.getType() == ShopType.ADMIN_SELL_SHOP) loooooonngg = loooooonngg.replaceAll("sell", "buy");

            player.sendMessage(loooooonngg);
            player.sendMessage(ChatColor.GRAY + "Use Shift + Right Click to restock the shop.");
            shop.setOutOfStock(false);
            shop.save();
        }
    }
}
