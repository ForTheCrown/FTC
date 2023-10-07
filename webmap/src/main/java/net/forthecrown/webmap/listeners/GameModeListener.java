package net.forthecrown.webmap.listeners;

import static net.forthecrown.webmap.HideSetting.DYNMAP_HIDE;
import static net.forthecrown.webmap.HideSetting.HIDDEN;

import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent.Cause;

public class GameModeListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
    if (event.getCause() == Cause.DEFAULT_GAMEMODE) {
      return;
    }

    Player player = event.getPlayer();
    User user = Users.get(player);

    GameMode oldMode = player.getGameMode();
    GameMode newMode = event.getNewGameMode();

    if (oldMode == GameMode.SPECTATOR && newMode != GameMode.SPECTATOR) {
      boolean hide = user.get(DYNMAP_HIDE);
      HIDDEN.setState(user, hide);
    }

    if (newMode == GameMode.SPECTATOR && oldMode != GameMode.SPECTATOR) {
      boolean hide = HIDDEN.getState(user);
      user.set(DYNMAP_HIDE, hide);
      HIDDEN.setState(user, true);
    }
  }
}
