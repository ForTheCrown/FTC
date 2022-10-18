package net.forthecrown.events.economy;

import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ShopInteractionListener implements Listener {

    @EventHandler
    public void onSignShopUser(PlayerInteractEvent event){
        // Not clicking block, not worth our time
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Interacted block is not a shop, stop
        if (!SignShops.isShop(event.getClickedBlock())) {
            return;
        }

        var player = event.getPlayer();

        //Can't use in spectator lol
        if(player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Test player being on a little cooldown
        if (Cooldown.containsOrAdd(player, 6)) {
            return;
        }

        ShopManager manager = Crown.getEconomy().getShops();
        SignShop shop = manager.getShop(event.getClickedBlock());

        //Handle interaction between player and shop
        manager.handleInteraction(Users.get(player), shop);
    }

}