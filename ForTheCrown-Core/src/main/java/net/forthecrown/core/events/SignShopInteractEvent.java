package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.customevents.SignShopUseEvent;
import net.forthecrown.core.files.Balances;
import net.forthecrown.core.files.FtcUser;
import net.forthecrown.core.files.SignShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SignShopInteractEvent implements Listener {

    private final Map<UUID, SignShop> dopfguijh = new HashMap<>(); //Yes! I rolled my face on the keyboard to get the name... V.2

    @EventHandler
    public void onSignShopUser(PlayerInteractEvent event){
        if(FtcCore.isOnCooldown(event.getPlayer())) return;
        if(event.getClickedBlock() == null) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=") && !sign.getLine(0).contains("-[Sell]-") && !sign.getLine(0).contains("-[Buy]-")) return;
        if(!sign.getLine(3).contains(ChatColor.DARK_GRAY + "Price: ")) return;

        if(event.isBlockInHand()){
            event.setCancelled(true);
        }
        FtcCore.addToCooldown(event.getPlayer(), 6, false);

        SignShop shop;
        try {
            shop = FtcCore.getSignShop(event.getClickedBlock().getLocation());
        } catch (NullPointerException e){
            e.printStackTrace();
            return;
        }
        Player player = event.getPlayer();

        //checks if they're the owner and if they're sneaking, then opens the shop inventory to edit it
        if(player.isSneaking() && (shop.getOwner() == player.getUniqueId() || player.hasPermission("ftc.admin"))){
            player.openInventory(shop.getShopInventory());
            dopfguijh.put(player.getUniqueId(), shop);
            return;
        }

        Balances bals = FtcCore.getBalances();
        FtcUser user = FtcCore.getUser(player.getUniqueId());

        FtcCore.getInstance().getServer().getPluginManager().callEvent(new SignShopUseEvent(shop, user, player, bals));
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event){ //items added
        if(event.getInventory().getType() != InventoryType.CHEST) return;
        if(!dopfguijh.containsKey(event.getPlayer().getUniqueId())) return;
        Player player = (Player) event.getPlayer();

        Inventory inv = event.getInventory();
        SignShop shop = dopfguijh.get(player.getUniqueId());
        dopfguijh.remove(player.getUniqueId());
        ItemStack[] invalidItems = shop.setItems(inv.getStorageContents()); //setItems returns an array of invalid items that couldn't be added to the shop inventory

        if(invalidItems != null && invalidItems.length > 0){
            for (ItemStack stack : invalidItems){
                if(player.getInventory().firstEmpty() == -1) player.getLocation().getWorld().dropItemNaturally(player.getLocation(), stack);
                else player.getInventory().addItem(stack);
            }
            player.sendMessage(FtcCore.getPrefix() + ChatColor.GRAY + "Unable to add items to shop!");
        }
    }
}