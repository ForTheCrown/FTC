package net.forthecrown.serverlist;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import java.util.Random;
import net.forthecrown.events.DayChangeEvent;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
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
    var base = display.getBaseMotd();

    if (base == null) {
      base = Bukkit.motd();
    }

    PlaceholderRenderer placeholders = Placeholders.newRenderer()
        .useDefaults()
        .add("version", Bukkit::getMinecraftVersion)
        .add("ip", event.getAddress().getHostName());

    placeholders.add("message", placeholders.render(pair.right()));

    event.motd(placeholders.render(base));

    if (pair.first() != null) {
      event.setServerIcon(pair.first());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onDayChange(DayChangeEvent event) {
    ServerListDisplay display = JavaPlugin.getPlugin(ServerlistPlugin.class).getDisplay();
    display.cacheDateEntries();
  }
}
