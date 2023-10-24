package net.forthecrown.guilds.listeners;

import java.util.Objects;
import net.forthecrown.Loggers;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.slf4j.Logger;

public class GuildMoveListener implements Listener {

  private static final Logger LOGGER = Loggers.getLogger();

  @EventHandler(ignoreCancelled = true)
  public void onChunkEnter(PlayerMoveEvent event) {
    // Add cooldown to limit amount ran per player
    if (Cooldown.containsOrAdd(event.getPlayer(), getClass().getSimpleName(), 4)
        // If player not in Guild World, return
        || !event.getTo().getWorld().equals(Guilds.getWorld())
    ) {
      // Jules: Combine 2 if statements into 1
      return;
    }

    var manager = Guilds.getManager();

    Guild guildFrom = manager.getChunkMap().get(event.getFrom());
    Guild guildTo = manager.getChunkMap().get(event.getTo());

    LOGGER.debug("GuildMoveListener triggered, from={}, to={}",
        guildFrom == null ? null : guildFrom.getName(),
        guildTo == null ? null : guildTo.getName()
    );

    // ? -> Guild
    if (guildTo != null) {
      // No message when moving between chunks of same guild
      if (Objects.equals(guildFrom, guildTo)) {
        return;
      }

      sendInfo(event.getPlayer(), guildTo, "Entering ");
    }
    // Guild -> Wild
    else if (guildFrom != null) {
      sendInfo(event.getPlayer(), guildFrom, "Leaving ");
    }
  }

  // Format a message and send to player's action bar
  private void sendInfo(Player player, Guild guild, String info) {
    player.sendActionBar(
        Component.text(info, NamedTextColor.GOLD)
            .append(guild.getPrefix())
    );
  }
}