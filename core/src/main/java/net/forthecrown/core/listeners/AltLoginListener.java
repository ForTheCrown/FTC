package net.forthecrown.core.listeners;

import java.util.Optional;
import java.util.UUID;
import net.forthecrown.core.CoreConfig.AltJoinPrevention;
import net.forthecrown.core.CorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class AltLoginListener implements Listener {

  private final CorePlugin plugin;

  public AltLoginListener(CorePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    AltJoinPrevention prevention = plugin.getFtcConfig().preventAltJoining();

    UUID id = event.getUniqueId();
    Optional<Component> denyReason = prevention.mayJoin(id, plugin.getUserService());

    denyReason.ifPresent(component -> {
      event.disallow(Result.KICK_OTHER, component);
    });
  }
}
