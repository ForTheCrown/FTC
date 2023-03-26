package net.forthecrown.events;

import net.forthecrown.grenadier.CommandBroadcastEvent;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CommandBroadcastListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onCommandBroadcast(CommandBroadcastEvent event) {
    event.setFormatter((viewer, message, source) -> {
      return Text.format("{0} &8&l»&r {1}",
          NamedTextColor.GRAY,
          Text.sourceDisplayName(source), message
      );
    });
  }
}