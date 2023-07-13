package net.forthecrown.serverlist;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerlistListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPaperServerListPing(PaperServerListPingEvent event) {
    ServerListDisplay display = JavaPlugin.getPlugin(ServerlistPlugin.class).getDisplay();

    if (display.isAllowMaxPlayerRandomization()) {
      Random random = display.getRandom();
      int max = Bukkit.getMaxPlayers();
      int newMax = random.nextInt(max, max + max / 2);
      event.setMaxPlayers(newMax);
    }

    var pair = display.getCurrent();
    event.motd(motd(pair.right()));

    if (pair.first() != null) {
      event.setServerIcon(pair.first());
    }
  }

  Component motd(Component afterDash) {
    return text()
        .color(NamedTextColor.GRAY)

        .append(
            text("For The Crown", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)),

            afterDash == null
                ? empty()
                : text(" - ").append(afterDash)
        )

        .append(newline())
        .append(text("Currently on " + Bukkit.getMinecraftVersion()))

        .build();
  }
}
