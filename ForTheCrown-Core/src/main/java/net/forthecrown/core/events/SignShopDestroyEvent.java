package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SignShopDestroyEvent implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSignDestroy(BlockBreakEvent event){
        if(!(event.getBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) event.getBlock().getState();
        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=")) return;
        if(!sign.getLine(3).contains(ChatColor.DARK_GRAY + "Price: ")) return;

        CrownSignShop shop;
        try {
            shop = FtcCore.getSignShop(event.getBlock().getLocation());
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        if(!shop.getOwner().equals(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
            throw new InvalidCommandExecution(event.getPlayer(), "&cYou cannot destroy a sign you do not own!");
        }

        shop.destroyShop(); //destroys the shop
    }
}
