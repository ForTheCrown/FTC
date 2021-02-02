package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.files.CrownSignShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignShopCreateEvent implements Listener {

    private final Map<UUID, CrownSignShop> asdasdaqsd = new HashMap<>();//Yes! I rolled my face on the keyboard to get the name

    //WHAT AM I DOING
    @EventHandler(ignoreCancelled = true)
    public void onSignShopCreate(SignChangeEvent event){
        if(event.getLine(0) == null || event.getLine(0).equals("")) return;

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

        if(event.getLine(3) == null || event.getLine(3).equals("")){
            player.sendMessage("You dumbass");
            return;
        }

        Sign sign = (Sign) event.getBlock().getState();
        String lastLine = event.getLine(3).toLowerCase();


        lastLine = lastLine.replaceAll("[\\D]", ""); //replaces all letter chars, leaves only numbers to make the price
        lastLine = lastLine.replaceAll(" ", ""); //removes all spaces

        int price;
        try {
            price = Integer.parseInt(lastLine);
        } catch (Exception e){ throw new CrownException(player, "&7The last line must contain numbers!"); }

        if(event.getLine(2).equals("") && event.getLine(1).equals("")) throw new CrownException(player, "&7You must provide a description of the shop's items");

        CrownSignShop shop = FtcCore.createSignShop(sign.getLocation(), shopType, price, player.getUniqueId()); //creates the signshop file

        player.openInventory(shop.getExampleInventory());
        asdasdaqsd.put(player.getUniqueId(), shop);

        if(shopType == ShopType.BUY_SHOP) event.setLine(0, shopType.getOutOfStockLabel());
        else event.setLine(0, shopType.getInStockLabel());

        event.setLine(3, ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + price); //idk, I thought putting ALL_CODES would make triggering events involving the shops harder
    }

    @EventHandler
    public void invClickEvent(InventoryClickEvent event){
        if(event.getCurrentItem() == null) return;
        if(!event.getView().getTitle().contains("Specify what and how much")) return;
        if(event.getCurrentItem().getType() == Material.BARRIER) event.setCancelled(true);
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

        ItemStack item = inv.getContents()[2];
        if(item == null){
            sign.setLine(3, ChatColor.DARK_GRAY + "Price " + ChatColor.RESET + shop.getPrice());
            sign.update();
            throw new CrownException(player, "&4Shop creation failed! &cNo item in the inventory");
        }

        shop.getStock().setExampleItem(item);
        Bukkit.broadcastMessage(shop.getStock().getExampleItem().toString());
        Bukkit.broadcastMessage(shop.getStock().getContents().toString());
    }
}
/*
        boolean trash = shop.dealWithExampleItems(inv);

        if(!trash){
            for (ItemStack stack : inv.getContents()){
                if (stack == null) continue;
                if(player.getInventory().firstEmpty() == -1) player.getWorld().dropItemNaturally(player.getLocation(), stack);
                else player.getInventory().addItem(stack);
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
                    shop.getExampleItem().getType().toString().toLowerCase().replaceAll("_", " ") +
                    " for " + shop.getPrice() +
                    " Rhines. Use Shift + Right Click to restock the shop.";

            player.sendMessage(loooooonngg);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            shop.setOutOfStock(false);
        }
 */
