package net.forthecrown.usables.listeners;

import com.google.common.base.Strings;
import net.forthecrown.Loggers;
import net.forthecrown.usables.UsablesPlugin;
import net.forthecrown.user.event.UserJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

class JoinListener implements Listener {

  public static final Logger LOGGER = Loggers.getLogger();

  private final UsablesPlugin plugin;

  public JoinListener(UsablesPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onUserJoin(UserJoinEvent event) {
    if (!event.isFirstJoin()) {
      return;
    }

    var kitName = plugin.getUsablesConfig().getFirstJoinKit();

    if (Strings.isNullOrEmpty(kitName)) {
      return;
    }

    var kit = plugin.getKits().get(kitName);

    if (kit == null) {
      LOGGER.warn("No kit named '{}' found, cannot give firstJoinKit", kitName);
    }

    kit.interact(event.getPlayer());
  }
}
