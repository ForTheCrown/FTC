package net.forthecrown.vikings;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.exceptions.InvalidBranchException;
import net.forthecrown.vikings.raids.RaidHandler;
import net.forthecrown.vikings.raids.VikingRaid;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class RaidChooseListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager)) return;
        if(event.getRightClicked().getCustomName() == null || !event.getRightClicked().getCustomName().contains("VIKING_RAID_PLACEHOLDER")) return;

        Villager villie = (Villager) event.getRightClicked();
        if(!villie.isInvulnerable()) return;

        event.setCancelled(true);

        CrownUser user = FtcCore.getUser(event.getPlayer().getUniqueId());
        if(!user.getBranch().equals(Branch.VIKINGS)) throw new InvalidBranchException(user, "Vikings");

        event.getPlayer().openInventory(VikingUtils.getRaidSelector());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!event.getView().getTitle().contains("Raid Selection")) return;
        if(event.getInventory().getSize() != 27) return;
        if(event.isShiftClick()){
            event.setCancelled(true);
            return;
        }
        if(event.getCurrentItem() == null) return;

        event.setCancelled(true);

        if(Cooldown.contains(event.getWhoClicked(), "VikingRaidSelector")) return;
        Cooldown.add(event.getWhoClicked(), "VikingRaidSelector", 6);

        VikingRaid raid = RaidHandler.fromName(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
        if(raid == null) return;

        Vikings.getRaidHandler().callRaid((Player) event.getWhoClicked(), raid);
    }
}
