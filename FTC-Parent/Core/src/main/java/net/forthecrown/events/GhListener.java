package net.forthecrown.events;

import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.grappling.GhLevelData;
import net.forthecrown.utils.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class GhListener implements Listener {
    private static final String COOLDOWN_CATEGORY = "Pirates_GH";

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(!event.getRightClicked().getPersistentDataContainer().has(Pirates.GH_STAND_KEY, PersistentDataType.STRING)) return;
        if(Cooldown.contains(event.getPlayer(), COOLDOWN_CATEGORY)) return;
        Cooldown.add(event.getPlayer(), COOLDOWN_CATEGORY, 20);

        String id = event.getRightClicked().getPersistentDataContainer().get(Pirates.GH_STAND_KEY, PersistentDataType.STRING);
        assert id != null : "Id was null";

        GhLevelData levelData = Pirates.getParkour().byName(id);
        if(levelData == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        levelData.exit(player, player.getWorld());
    }
}