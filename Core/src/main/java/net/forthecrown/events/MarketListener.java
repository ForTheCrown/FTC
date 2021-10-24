package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.ShopEntrance;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class MarketListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock().getType() != Material.PLAYER_HEAD) return;

        Skull skull = (Skull) event.getClickedBlock().getState();
        if(!skull.getPersistentDataContainer().has(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING)) return;

        String shopName = skull.getPersistentDataContainer().get(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING);
        Markets markets = Crown.getMarkets();
        MarketShop shop = markets.get(shopName);

        openPurchaseBook(markets, shop, UserManager.getUser(event.getPlayer()));
    }

    private void openPurchaseBook(Markets markets, MarketShop shop, CrownUser user) {

    }
}
