package net.forthecrown.core.economy.events;

import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.economy.files.SignShop;
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
        if(!sign.getLine(3).contains("Price: ")) return;

        SignShop shop;
        try {
            shop = Economy.getSignShop(event.getBlock().getLocation());
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        shop.destroyShop();
    }
}
