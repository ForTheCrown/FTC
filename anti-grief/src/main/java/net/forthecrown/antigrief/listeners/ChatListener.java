package net.forthecrown.antigrief.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.antigrief.Mute;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.antigrief.StaffChat;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

class ChatListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onAsyncChat(AsyncChatEvent event) {
    Mute mute = Punishments.checkMute(event.getPlayer());

    if (mute == Mute.HARD) {
      event.setCancelled(true);
      return;
    }

    if (mute == Mute.SOFT) {
      event.viewers().removeIf(audience -> !audience.equals(event.getPlayer()));
    }

    if (BannedWords.checkAndWarn(event.getPlayer(), event.message())) {
      event.setCancelled(true);
    }

    if (StaffChat.toggledPlayers.contains(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
      User user = Users.get(event.getPlayer());

      StaffChat.newMessage()
          .setLogged(true)
          .setDiscordForwarded(!user.get(StaffChat.FORWARDING_DISABLED))
          .setMessage(ViewerAwareMessage.wrap(event.message()))
          .setSource(user.getCommandSource())
          .send();
    }
  }
}