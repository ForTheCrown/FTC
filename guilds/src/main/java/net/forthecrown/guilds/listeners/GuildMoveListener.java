package net.forthecrown.guilds.listeners;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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

  // Map<PlayerId, GuildId>, mapping players to the guild that owns the chunk they're currently located in
  private final static Map<UUID, UUID> MAP = new Object2ObjectOpenHashMap<>();

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

    UUID playerId = event.getPlayer().getUniqueId();
    UUID guildIdFrom = MAP.get(playerId);

    Guild guildTo = manager.getOwner(Guilds.getChunk(event.getTo()));
    Guild guildFrom = manager.getGuild(guildIdFrom);

    // ? -> Guild
    if (guildTo != null) {
      // No message when moving between chunks of same guild
      // Jules: use Objects.equals(o, o1) to minimize MAP lookups
      if (Objects.equals(MAP.get(playerId), guildTo.getId())) {
        return;
      }

      sendInfo(event.getPlayer(), guildTo, "Entering ");
      MAP.put(playerId, guildTo.getId());
    }
    // Guild -> Wild
    else if (guildIdFrom != null) {
      sendInfo(event.getPlayer(), guildFrom, "Leaving ");
      MAP.remove(playerId);
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