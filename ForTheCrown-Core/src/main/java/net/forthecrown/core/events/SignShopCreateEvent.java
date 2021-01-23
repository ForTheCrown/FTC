package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.files.SignShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignShopCreateEvent implements Listener {

    private final Map<UUID, SignShop> asdasdaqsd = new HashMap<>();//Yes! I rolled my face on the keyboard to get the name

    //WHAT AM I DOING
    @EventHandler(ignoreCancelled = true)
    public void onSignShopCreate(SignChangeEvent event){
        if(event.getLine(0) == null) return;
        if(event.getLine(3) == null) return;

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

        Sign sign = (Sign) event.getBlock().getState();
        String lastLine = event.getLine(3).toLowerCase();

        lastLine = lastLine.replaceAll("[\\D]", ""); //replaces all letter chars, leaves only numbers to make the price
        lastLine = lastLine.replaceAll(" ", ""); //removes all spaces

        int price;
        try {
            price = Integer.parseInt(lastLine);
        } catch (Exception e){
            return;
        }

        if(event.getLine(2) == null && event.getLine(1) == null) {
            player.sendMessage("You must provide a description of the item");
            return;
        }

        SignShop shop = FtcCore.createSignShop(sign.getLocation(), shopType, price, player.getUniqueId()); //creates the signshop file

        player.openInventory(shop.getExampleInventory());
        asdasdaqsd.put(player.getUniqueId(), shop);

        if(shopType == ShopType.BUY_SHOP) event.setLine(0, shopType.getOutOfStockLabel());
        else event.setLine(0, shopType.getInStockLabel());

        event.setLine(3, ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + price); //idk, I thought putting ALL_CODES would make triggering events involving the shops harder
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){ //sets the example item and adds the item(s) to the shop's inventory
        if(!asdasdaqsd.containsKey(event.getPlayer().getUniqueId())) return;
        if(event.getInventory().getType() != InventoryType.HOPPER) return;
        Player player = (Player) event.getPlayer();

        SignShop shop = asdasdaqsd.get(player.getUniqueId());
        asdasdaqsd.remove(player.getUniqueId());
        Sign sign = (Sign) shop.getBlock().getState();

        Inventory inv = event.getInventory();
        if(!shop.setExampleItems(inv.getContents())){
            for (ItemStack stack : inv.getContents()){
                if (stack == null) continue;
                player.getInventory().addItem(stack);
            }
            player.sendMessage(ChatColor.DARK_RED + "SignShop creation failed!" + ChatColor.RED + " Please only specify one item stack, not several");
            sign.setLine(0, ChatColor.DARK_RED + ChatColor.stripColor(shop.getType().getInStockLabel()));
            sign.setLine(3, sign.getLine(3).replaceAll(":", ""));
            sign.update();
            shop.destroyShop();
        } else {
            String loooooonngg = ChatColor.GREEN + "SignShop created!" + ChatColor.RESET + " It'll " +
                    shop.getType().toString().toLowerCase().replaceAll("_shop", "").replaceAll("admin_", "") + " " +
                    shop.getExampleItem().getAmount() + " " +
                    shop.getExampleItem().getType().toString().toLowerCase() +
                    " for " + shop.getPrice() +
                    " Rhines. Use Shift + Right Click to restock the shop.";

            player.sendMessage(loooooonngg);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            shop.setOutOfStock(false);
        }
    }
}
