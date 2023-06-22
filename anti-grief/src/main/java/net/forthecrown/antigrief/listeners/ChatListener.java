package net.forthecrown.antigrief.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.antigrief.Mute;
import net.forthecrown.antigrief.Punishments;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class ChatListener implements Listener {

  @EventHandler(ignoreCancelled = true)
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
  }
}