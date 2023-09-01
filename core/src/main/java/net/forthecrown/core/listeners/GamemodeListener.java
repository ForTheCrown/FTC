package net.forthecrown.core.listeners;

import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent.Cause;

public class GamemodeListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
    // May not be fully online at this point
    if (event.getCause() == Cause.DEFAULT_GAMEMODE) {
      return;
    }

    Tasks.runLater(() -> {
      User user = Users.get(event.getPlayer());
      user.updateFlying();
    }, 1);
  }
}
