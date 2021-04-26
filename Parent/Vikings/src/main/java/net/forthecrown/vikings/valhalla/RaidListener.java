package net.forthecrown.vikings.valhalla;

import net.forthecrown.vikings.Vikings;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RaidListener implements Listener {

    private final VikingRaid raid;
    private final RaidParty party;
    private final Vikings main;

    public RaidListener(VikingRaid raid, RaidParty party, Vikings main) {
        this.raid = raid;
        this.party = party;
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!party.getParticipants().contains(event.getEntity())) return;
        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(main, () -> party.leave(event.getEntity()), 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!party.getParticipants().contains(event.getPlayer())) return;
        party.leave(event.getPlayer());
    }
}
