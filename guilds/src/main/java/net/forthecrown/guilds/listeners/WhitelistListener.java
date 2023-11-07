package net.forthecrown.guilds.listeners;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.utils.Tasks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WhitelistListener implements Listener {

  private final GuildManager manager;

  public WhitelistListener(GuildManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true)
  public void onWhitelistToggle(WhitelistToggleEvent event) {
    Tasks.runLater(() -> {
      manager.getExpModifier().updateTickingState();
    }, 1);
  }
}