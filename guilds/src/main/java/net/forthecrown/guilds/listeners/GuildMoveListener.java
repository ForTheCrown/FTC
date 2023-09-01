package net.forthecrown.guilds.listeners;

import java.util.Objects;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class GuildMoveListener implements Listener {

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

    // ? -> Guild
    if (guildTo != null) {
      // No message when moving between chunks of same guild
      // Jules: use Objects.equals(o, o1) to minimize MAP lookups
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
    // Jules: Add braces to if statement
    if (guild == null) {
      return; // shouldn't happen?
      // Jules: Might happen if the guild has been removed,
      // or if chunk was unclaimed
    }

    player.sendActionBar(Component.text(info)
        .color(NamedTextColor.GOLD)
        .append(guild.getPrefix())
    );
  }

  public static boolean isInOwnGuild(Player player) {
    Guild guild = Guilds.getStandingInOwn(player);
    return guild != null;
  }
}