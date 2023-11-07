package net.forthecrown.core.listeners;

import net.forthecrown.Loggers;
import net.forthecrown.grenadier.CommandBroadcastEvent;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

public class AdminBroadcastListener implements Listener {

  public static final Logger LOGGER = Loggers.getLogger();

  @EventHandler
  public void onCommandBroadcast(CommandBroadcastEvent event) {
    event.setFormatter((viewer, message, source) -> {
      Component displayName;

      if (!source.isPlayer()) {
        displayName = source.displayName();
      } else {
        User user = Users.get(source.asPlayerOrNull());
        displayName = user.displayName(viewer);
      }

      return Text.format("&8{0} &l»&r {1}", NamedTextColor.GRAY, displayName, message);
    });
  }
}
