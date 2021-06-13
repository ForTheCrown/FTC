package net.forthecrown.core.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.core.Announcer;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserInteractions;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.user.data.MarriageMessage;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;
import java.util.logging.Level;

public class ChatEvents implements Listener {
    public static final World SENATE_WORLD = Objects.requireNonNull(Bukkit.getWorld("world_senate"));

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();

        // Tell the Player where they died, but ignore world_void deaths.
        String diedAt = "died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ".";
        if (!loc.getWorld().getName().equalsIgnoreCase("world_void"))
            player.sendMessage(ChatColor.GRAY + "[FTC] You " + diedAt);

        Announcer.log(Level.INFO, "! " + player.getName() + " " + diedAt);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        event.composer(ChatFormatter::formatChat);

        PunishmentManager punishments = CrownCore.getPunishmentManager();
        Player player = event.getPlayer();
        MuteStatus status = punishments.checkMute(player);

        if(status != MuteStatus.NONE){
            event.recipients().removeIf(p -> !punishments.isSoftmuted(p.getUniqueId()));
            EavesDropper.reportMuted(event.message(), player, status);

            if(status == MuteStatus.HARD) event.setCancelled(true);
            return;
        }

        if (StaffChat.toggledPlayers.contains(player)) {
            if(StaffChat.ignoring.contains(player)){
                player.sendMessage(Component.text("You are ignoring staff chat, do '/sct visible' to use it again").color(NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            event.recipients().removeIf(p -> !p.hasPermission(Permissions.STAFF_CHAT) || StaffChat.ignoring.contains(p));
            return;
        }

        if(player.getWorld().equals(SENATE_WORLD)){
            event.recipients().removeIf(p -> !p.getWorld().equals(SENATE_WORLD));
            return;
        }

        //Remove ignored
        event.recipients().removeIf(p -> {
            UserInteractions inter = UserManager.getUser(p).getInteractions();

            return inter.isBlockedPlayer(player.getUniqueId());
        });

        CrownUser user = UserManager.getUser(player);
        UserInteractions inter = user.getInteractions();

        if(inter.marriageChatToggled()){
            event.recipients().clear();

            new MarriageMessage(user, UserManager.getUser(inter.getMarriedTo()), ChatUtils.getString(event.message()))
                    .complete();
        }
    }
}