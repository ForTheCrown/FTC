package net.forthecrown.serverlist;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.google.common.base.Strings;
import java.util.Random;
import net.forthecrown.events.DayChangeEvent;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerlistListener implements Listener {

  private final ServerlistPlugin plugin;

  public ServerlistListener(ServerlistPlugin plugin) {
    this.plugin = plugin;
  }

  void logPing(PaperServerListPingEvent event) {
    var logger = plugin.getSLF4JLogger();
    if (!logger.isDebugEnabled()) {
      return;
    }

    var users = Users.getService();
    var profile = users.getLookup().query(event.getAddress().getHostAddress());

    if (profile == null) {
      logger.debug("Received server ping from IP {}", event.getAddress().getAddress());
    } else {
      logger.debug("Received server ping from player {} (IP={})",
          profile.getName(), event.getAddress().getAddress()
      );
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPaperServerListPing(PaperServerListPingEvent event) {
    logPing(event);

    ServerListDisplay display = plugin.getDisplay();
    var config = plugin.getListConfig();

    if (config.appearOffline()) {
      event.setCancelled(true);
      return;
    }

    int playerCountRange = config.maxPlayerRandomRange();

    if (playerCountRange > 0) {
      Random random = display.getRandom();
      int max = Bukkit.getMaxPlayers();
      int newMax = random.nextInt(max, max + playerCountRange);
      event.setMaxPlayers(newMax);
    }

    ListDisplayData pair = display.getCurrent();
    var base = config.baseMotd() == null ? null : Text.valueOf(config.baseMotd());

    if (base == null) {
      base = Bukkit.motd();
    }

    PlaceholderRenderer placeholders = Placeholders.newRenderer()
        .useDefaults()
        .add("version", Bukkit::getMinecraftVersion)
        .add("ip", event.getAddress().getHostAddress());

    var service = Users.getService();
    var lookup = service.getLookup();

    var entry = lookup.query(event.getAddress().getHostAddress());

    User user;

    if (entry != null && config.inferPlayerBasedOffIp() && service.userLoadingAllowed()) {
      user = service.getUser(entry);
    } else {
      user = null;
    }

    placeholders.add("message", placeholders.render(pair.motdPart, user));
    event.motd(placeholders.render(base, user));

    if (pair.icon != null) {
      event.setServerIcon(pair.icon);
    }

    if (pair.protocolOverride > 0 && config.allowChangingProtocolVersions()) {
      event.setProtocolVersion(pair.protocolOverride);
    }

    if (!Strings.isNullOrEmpty(pair.versionText) && config.allowChangingVersionText()) {
      String text = pair.versionText;
      Component versionBase = placeholders.render(Text.valueOf(text, user), user);
      event.setVersion(Text.SECTION_LEGACY.serialize(versionBase));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onDayChange(DayChangeEvent event) {
    ServerListDisplay display = JavaPlugin.getPlugin(ServerlistPlugin.class).getDisplay();
    display.cacheDateEntries();
  }
}
