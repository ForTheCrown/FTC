package net.forthecrown.core.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServerPingListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPaperServerListPing(PaperServerListPingEvent event) {
    var it = event.iterator();

    while (it.hasNext()) {
      Player player = it.next();
      User user = Users.get(player);

      if (user.get(Properties.VANISHED)) {
        it.remove();
      }
    }
  }
}
