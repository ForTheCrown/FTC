package net.forthecrown.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.data.MarriageMessage;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatEvents implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        event.renderer(ChatFormatter::formatChat);

        //WHO THE FUCK THOUGHT A  SET OF VIEWERS WAS BETTER THAN AN EASILY ITERABLE AND CHANGEABLE SET OF
        //PLAYERS, FUCK YOU ALL

        PunishmentManager punishments = CrownCore.getPunishmentManager();
        Player player = event.getPlayer();
        MuteStatus status = punishments.checkMute(player);

        if(status != MuteStatus.NONE){
            event.viewers().removeIf(a -> {
                Player p = CrownUtils.fromAudience(a);
                if(p == null) return false;

                return  !punishments.isSoftmuted(p.getUniqueId());
            });
            EavesDropper.reportMuted(event.message(), player, status);

            if(status == MuteStatus.HARD) event.setCancelled(true); //Completely cancel if they are hardmuted, checkMute sends them mute message
            return;
        }

        if (StaffChat.toggledPlayers.contains(player)) {
            if(StaffChat.ignoring.contains(player)){
                player.sendMessage(Component.text("You are ignoring staff chat, do '/sct visible' to use it again").color(NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            event.viewers().removeIf(a -> {
                Player p = CrownUtils.fromAudience(a);
                if(p == null) return false;

                return  !p.hasPermission(Permissions.STAFF_CHAT) || StaffChat.ignoring.contains(p);
            });
            return;
        }

        if(player.getWorld().equals(Worlds.SENATE)){
            event.viewers().removeIf(a -> {
                Player p = CrownUtils.fromAudience(a);
                if(p == null) return false;

                return !p.getWorld().equals(Worlds.SENATE);
            });
            return;
        } else event.viewers().removeIf(a -> {
            Player p = CrownUtils.fromAudience(a);
            if(p == null) return false;

            return p.getWorld().equals(Worlds.SENATE);
        });

        //Remove ignored
        event.viewers().removeIf(a -> {
            Player p = CrownUtils.fromAudience(a);
            if(p == null) return false;

            UserInteractions inter = UserManager.getUser(p).getInteractions();

            return inter.isBlockedPlayer(player.getUniqueId());
        });

        CrownUser user = UserManager.getUser(player);
        UserInteractions inter = user.getInteractions();

        if(inter.marriageChatToggled()){
            event.viewers().clear();

            CrownUser target = UserManager.getUser(inter.getMarriedTo());
            if(!target.isOnline()){
                user.sendMessage(
                        Component.translatable(UserType.USER_NOT_ONLINE.getTranslationKey(), target.nickDisplayName())
                                .color(NamedTextColor.RED)
                );
                return;
            }

            new MarriageMessage(user, UserManager.getUser(inter.getMarriedTo()), ChatUtils.getString(event.message()))
                    .complete();
        }
    }
}