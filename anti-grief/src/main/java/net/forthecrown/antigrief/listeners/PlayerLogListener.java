package net.forthecrown.antigrief.listeners;

import net.forthecrown.user.Properties;
import net.forthecrown.user.event.UserJoinEvent;
import net.forthecrown.user.event.UserLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class PlayerLogListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onUserJoin(UserJoinEvent event) {
    if (event.getUser().get(Properties.VANISHED)) {
      event.setShowMessage(false);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onUserLeave(UserLeaveEvent event) {
    if (event.getUser().get(Properties.VANISHED)) {
      event.setShowMessage(false);
    }
  }
}
