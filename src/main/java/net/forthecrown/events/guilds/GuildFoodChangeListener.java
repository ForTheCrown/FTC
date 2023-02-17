package net.forthecrown.events.guilds;

import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.user.Users;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class GuildFoodChangeListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onFoodLevelChange(FoodLevelChangeEvent event) {
    var player = event.getEntity();

    if (!GuildMoveListener.isInOwnGuild(player.getUniqueId())) {
      return;
    }

    var guild = Users.get(player.getUniqueId()).getGuild();

    if (!guild.hasActiveEffect(UnlockableChunkUpgrade.SATURATION)) {
      return;
    }

    if (player.getFoodLevel() > event.getFoodLevel()) {
      player.setSaturation(2);
    }
  }
}