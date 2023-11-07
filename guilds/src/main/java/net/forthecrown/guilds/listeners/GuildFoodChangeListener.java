package net.forthecrown.guilds.listeners;

import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class GuildFoodChangeListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onFoodLevelChange(FoodLevelChangeEvent event) {
    var player = (Player) event.getEntity();
    var guild = Guilds.getStandingInOwn(player);

    if (guild == null || !guild.hasActiveEffect(UnlockableChunkUpgrade.SATURATION)) {
      return;
    }

    if (player.getFoodLevel() > event.getFoodLevel()) {
      player.setSaturation(2);
    }
  }
}