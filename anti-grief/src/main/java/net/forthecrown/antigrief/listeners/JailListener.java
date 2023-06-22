package net.forthecrown.antigrief.listeners;

import net.forthecrown.antigrief.JailCell;
import net.forthecrown.antigrief.PunishmentManager;
import net.forthecrown.antigrief.Punishments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class JailListener implements Listener {

  static final Component NO_COMMANDS_IN_JAIL
      = Component.text("Cannot use commands while jailed", NamedTextColor.RED);

  private final PunishmentManager punisher;

  public JailListener() {
    punisher = Punishments.get();
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    JailCell cell = punisher.getCell(event.getPlayer().getUniqueId());

    if (cell == null) {
      return;
    }

    Player player = event.getPlayer();

    if (!cell.getCell().contains(player)) {
      var pos = cell.getPos();
      player.teleport(new Location(cell.getWorld(), pos.x(), pos.y(), pos.z()));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    JailCell cell = punisher.getCell(event.getPlayer().getUniqueId());

    if (cell == null) {
      return;
    }

    event.setCancelled(true);
    event.getPlayer().sendMessage(NO_COMMANDS_IN_JAIL);
  }
}