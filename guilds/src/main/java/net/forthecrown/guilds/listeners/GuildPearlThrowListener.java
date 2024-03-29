package net.forthecrown.guilds.listeners;

import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.kyori.adventure.sound.Sound;
import org.bukkit.GameMode;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class GuildPearlThrowListener implements Listener {
  private static final Sound SOUND = Sound.sound()
      .type(org.bukkit.Sound.ENTITY_ITEM_PICKUP)
      .volume(0.1F)
      .build();

  @EventHandler(ignoreCancelled = true)
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
    if (!(event.getEntity() instanceof EnderPearl pearl)
        || !(pearl.getShooter() instanceof Player player)
        || player.getGameMode() == GameMode.CREATIVE
    ) {
      return;
    }

    var guild = Guilds.getStandingInOwn(player);

    if (guild == null || !guild.hasActiveEffect(UnlockableChunkUpgrade.ENDERPEARL_REPLENISH)) {
      return;
    }

    player.getInventory().addItem(pearl.getItem());
    player.playSound(SOUND);
  }
}