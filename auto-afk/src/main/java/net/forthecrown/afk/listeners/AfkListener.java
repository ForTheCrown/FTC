package net.forthecrown.afk.listeners;

import net.forthecrown.afk.Afk;
import net.forthecrown.user.Users;
import net.forthecrown.user.event.UserLeaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkListener implements Listener {

  public static void checkUnafk(PlayerEvent event) {
    checkUnafk(event.getPlayer());
  }

  public static void checkUnafk(Player player) {
    var user = Users.get(player);
    if (!Afk.isAfk(user)) {
      return;
    }
    Afk.unafk(user);
  }

  @EventHandler(ignoreCancelled = true)
  public void onUserLeave(UserLeaveEvent event) {
    Afk.setAfk(event.getUser(), false, null);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!event.hasChangedOrientation()) {
      return;
    }

    checkUnafk(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    if (!event.getMessage().startsWith("/afk") && !event.getMessage().startsWith("afk")) {
      checkUnafk(event);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    checkUnafk(event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    checkUnafk(event.getEntity());
  }
}
