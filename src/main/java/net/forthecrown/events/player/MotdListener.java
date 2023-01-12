package net.forthecrown.events.player;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import java.util.Iterator;
import net.forthecrown.core.ServerListDisplay;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MotdListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPaperServerListPing(PaperServerListPingEvent event) {
    if (GeneralConfig.allowMaxPlayerRandomization) {
      int max = Bukkit.getMaxPlayers();
      int newMax = Util.RANDOM.nextInt(max, max + max / 2);

      event.setMaxPlayers(newMax);
    }

    var pair = ServerListDisplay.getInstance().getCurrent();
    event.motd(motd(pair.right()));

    if (pair.first() != null) {
      event.setServerIcon(pair.first());
    }

    Iterator<Player> iterator = event.iterator();
    while (iterator.hasNext()) {
      User user = Users.get(iterator.next());

      // Remove vanished players from
      // preview
      if (user.get(Properties.VANISHED)) {
        iterator.remove();
      }
    }
  }

  Component motd(Component afterDash) {
    return text()
        .color(NamedTextColor.GRAY)

        .append(text(
            "For The Crown",
            Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)
        ))
        .append(
            afterDash == null
                ? empty()
                : text(" - ").append(afterDash)
        )

        .append(newline())
        .append(text("Currently on " + Bukkit.getMinecraftVersion()))

        .build();
  }
}