package net.forthecrown.events.economy;

import net.forthecrown.economy.sell.ItemSeller;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserShopData;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class SellShopListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        var user = Users.get(player);
        var earnings = user.getComponent(UserShopData.class);
        var item = event.getItem();
        var stack = item.getItemStack();

        if (!earnings.getAutoSelling().contains(stack.getType())) {
            return;
        }

        ItemSeller.itemPickup(user, stack).run();

        if (stack.getAmount() <= 0) {
            event.setCancelled(true);
            item.remove();
        } else {
            event.getItem().setItemStack(stack);

            player.playSound(
                    Sound.sound(
                            org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            Sound.Source.MASTER,
                            2F, 1F
                    )
            );
        }
    }
}