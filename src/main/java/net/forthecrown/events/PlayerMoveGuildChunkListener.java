package net.forthecrown.events;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;
import java.util.UUID;

public class PlayerMoveGuildChunkListener implements Listener {

    // Map<PlayerId, GuildId>, mapping players to the guild that owns the chunk they're currently located in
    private final static Object2ObjectMap<UUID, UUID> MAP = new Object2ObjectOpenHashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onChunkEnter(PlayerMoveEvent event) {
        // Add cooldown to limit amount ran per player
        if (Cooldown.containsOrAdd(event.getPlayer(), getClass().getSimpleName(), 4)
                // If player not in Guild World, return
                || !event.getPlayer().getWorld().equals(Guilds.getWorld())
        ) {
            // Jules: Combine 2 if statements into 1
            return;
        }

        UUID playerId = event.getPlayer().getUniqueId();
        UUID guildIdFrom = MAP.get(playerId);
        Guild guildTo = GuildManager.get().getOwner(Guilds.getChunk(event.getTo()));

        // ? -> Guild
        if (guildTo != null) {
            // No message when moving between chunks of same guild
            if (Objects.equals(MAP.get(playerId), guildTo.getId())) { // Jules: use Objects.equals(o, o1) to minimize MAP lookups
                return;
            }

            sendInfo(event.getPlayer(), guildTo.getId(), "Entering ");
            MAP.put(playerId, guildTo.getId());
        }
        // Guild -> Wild
        else if (guildIdFrom != null) {
            sendInfo(event.getPlayer(), guildIdFrom, "Leaving ");
            MAP.remove(playerId);
        }
    }

    // Format a message and send to player's action bar
    private void sendInfo(Player player, UUID guildId, String info) {
        Guild guild = GuildManager.get().getGuild(guildId);

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

    public static boolean isInOwnGuild(UUID playerId) {
        // Jules: Use user variable instead of user ID lookups
        var user = Users.getLoadedUser(playerId);

        if (user == null) {
            return false;
        }

        if (user.getGuildId() == null) {
            return false;
        }

        // Jules: use Objects.equals(o, o1) to minimize MAP lookups
        return Objects.equals(MAP.get(playerId), user.getGuildId());
    }
}