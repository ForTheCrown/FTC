package net.forthecrown.events.guilds;

import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.user.Users;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class GuildFallListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getCause() != DamageCause.FALL
        || !(event.getEntity() instanceof Player player)
        || !GuildMoveListener.isInOwnGuild(player.getUniqueId())
    ) {
      return;
    }

    var guild = Users.get(player).getGuild();

    if (!guild.hasActiveEffect(UnlockableChunkUpgrade.FEATHER_FALL)) {
      return;
    }

    event.setDamage(DamageModifier.ABSORPTION, -4);

    Particle.REDSTONE.builder()
        .location(player.getLocation().add(0, 0.2, 0))
        .count(4)
        .offset(0.2, 0.2, 0.2)
        .extra(0)
        .color(Color.WHITE, 2)
        .spawn();
  }
}