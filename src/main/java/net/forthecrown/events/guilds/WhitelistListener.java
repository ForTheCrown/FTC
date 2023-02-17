package net.forthecrown.events.guilds;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.utils.Tasks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WhitelistListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onWhitelistToggle(WhitelistToggleEvent event) {
    Tasks.runLater(() -> {
      GuildManager.get()
          .getExpModifier()
          .updateTickingState();
    }, 1);
  }
}