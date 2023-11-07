package net.forthecrown.cosmetics.listeners;

import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.DeathEffect;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    User user = Users.get(event.getPlayer());
    CosmeticData data = user.getComponent(CosmeticData.class);
    DeathEffect effect = data.getValue(Cosmetics.DEATH_EFFECTS);

    if (effect == null) {
      return;
    }

    effect.activate(user.getLocation());
  }
}