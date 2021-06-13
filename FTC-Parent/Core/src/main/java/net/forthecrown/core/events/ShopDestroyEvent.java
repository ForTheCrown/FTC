package net.forthecrown.core.events;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.economy.shops.ShopManager;
import net.forthecrown.core.economy.shops.SignShop;
import net.forthecrown.core.CrownException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ShopDestroyEvent implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSignDestroy(BlockBreakEvent event) throws CrownException {
        if(!ShopManager.isShop(event.getBlock())) return;

        SignShop shop = CrownCore.getShopManager().getShop(event.getBlock().getLocation());
        if(shop == null) return;
        event.setCancelled(true);

        if(!shop.getOwner().equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("ftc.admin"))
            throw new CrownException(event.getPlayer(), "&cYou cannot destroy a shop you do not own!");

        Bukkit.getScheduler().runTaskLater(CrownCore.inst(), () -> shop.destroy(true), 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent event) {
        if(ShopManager.isShop(event.getBlock())) event.setCancelled(true);
    }

}
