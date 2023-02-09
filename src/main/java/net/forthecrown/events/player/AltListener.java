package net.forthecrown.events.player;

import java.util.Objects;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class AltListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    if (!GeneralConfig.enforceAltRule) {
      return;
    }

    var alts = UserManager.get().getAlts();
    var alreadyOnlineOpt = alts.getOtherAccounts(event.getUniqueId())
        .stream()
        .map(Bukkit::getPlayer)
        .filter(Objects::nonNull)
        .findAny();

    if (alreadyOnlineOpt.isEmpty()) {
      return;
    }

    event.disallow(
        Result.KICK_OTHER,
        Text.format(
            "Your other account ({1, user}) is already online",
            alreadyOnlineOpt.get()
        )
    );
  }
}