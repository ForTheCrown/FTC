package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ShopDestroyEvent implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSignDestroy(BlockBreakEvent event){
        if(!(event.getBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) event.getBlock().getState();

        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=")
            && !sign.getLine(0).contains("-[Buy]-") && !sign.getLine(0).contains("-[Sell]-")) return;
        if(!sign.getLine(3).contains(ChatColor.DARK_GRAY + "Price: ")) return;

        SignShop shop = FtcCore.getShop(event.getBlock().getLocation());
        if(shop == null) return;

        event.setCancelled(true);

        if(!shop.getOwner().equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("ftc.admin"))
            throw new CrownException(event.getPlayer(), "&cYou cannot destroy a shop you do not own!");

        shop.destroyShop(); //destroys the shop
    }
}
