package net.forthecrown.core.economy.events;

import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.economy.files.SignShop;
import net.forthecrown.core.enums.ShopType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignShopCreateEvent implements Listener {

    private final Map<UUID, SignShop> asdasdaqsd = new HashMap<>();

    //WHAT THE FUCK AM I DOING
    @EventHandler(ignoreCancelled = true)
    public void onSignShopCreate(SignChangeEvent event){
        if(event.getLine(0) == null) return;
        if(event.getLine(3) == null) return;

        Player player = event.getPlayer();

        ShopType shopType;

        switch (event.getLine(0)){
            default: return;

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
        Bukkit.broadcastMessage(shopType.toString());

        lastLine = lastLine.replaceAll("[\\D]", "");

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

        SignShop shop = Economy.createSignShop(sign.getLocation(), shopType, price, player.getUniqueId());

        player.openInventory(shop.getExampleInventory());
        asdasdaqsd.put(player.getUniqueId(), shop);

        String setFirstLine;
        switch (shopType){
            default:
                player.sendMessage(ChatColor.RED + "Shop creation failed at line 73. Tell an admin!");
                return;
            case ADMIN_BUY_SHOP:
                setFirstLine = ChatColor.AQUA + "=[Buy]=";
                break;
            case BUY_SHOP:
                setFirstLine = ChatColor.RED + "=[Buy]=";
                break;
            case SELL_SHOP:
                setFirstLine = ChatColor.RED + "=[Sell]=";
                break;
            case ADMIN_SELL_SHOP:
                setFirstLine = ChatColor.AQUA + "=[Sell]=";
                break;
        }
        event.setLine(0, setFirstLine);
        event.setLine(3, ChatColor.DARK_GRAY + "Price: " + ChatColor.WHITE + "$" + price);

        player.sendMessage(ChatColor.GREEN + "Shop creation successful!");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(!asdasdaqsd.containsKey(event.getPlayer().getUniqueId())) return;
        if(event.getInventory().getType() != InventoryType.HOPPER) return;
        Player player = (Player) event.getPlayer();

        SignShop shop = asdasdaqsd.get(player.getUniqueId());
        asdasdaqsd.remove(player.getUniqueId());

        Inventory inv = event.getInventory();
        shop.setExampleItems(inv.getContents());
    }
}
