package net.forthecrown.vikings;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.vikings.inventory.RaidSelector;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class VikingListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager)) return;
        if(event.getRightClicked().getCustomName() == null || !event.getRightClicked().getCustomName().contains("VIKING_RAID_PLACEHOLDER")) return;

        Villager villie = (Villager) event.getRightClicked();
        if(!villie.isInvulnerable()) return;

        event.setCancelled(true);

        CrownUser user = FtcCore.getUser(event.getPlayer().getUniqueId());
        if(!user.getBranch().equals(Branch.VIKINGS)) throw new CrownException(user, "Only Vikings can do this!");

        event.getPlayer().openInventory(new RaidSelector(FtcCore.getUser(event.getPlayer())).getInventory());
    }
}
