package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.inventories.SellShop;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CoreListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        FtcCore.getUser(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        FtcCore.getUser(event.getPlayer()).unload();
    }

    @EventHandler
    public void onServerShopNpcUse(PlayerInteractEntityEvent event){
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getRightClicked().getType() != EntityType.WANDERING_TRADER) return;
        LivingEntity trader = (LivingEntity) event.getRightClicked();

        if(trader.hasAI() && trader.getCustomName() == null || !trader.getCustomName().contains("Server Shop")) return;

        event.getPlayer().openInventory(new SellShop(event.getPlayer()).mainMenu());
        event.setCancelled(true);
    }
}
