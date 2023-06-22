package net.forthecrown.antigrief.listeners;

import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.antigrief.Mute;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.events.ChannelMessageEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class ChannelMessageListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onChannelMessage(ChannelMessageEvent event) {
    var source = event.getUserSource();

    if (source == null) {
      return;
    }

    Mute mute = Punishments.checkMute(source);

    if (mute == Mute.HARD) {
      event.setCancelled(true);
      return;
    }

    if (mute == Mute.SOFT) {
      event.getViewers().removeIf(audience -> audience.equals(event.getSource()));
    }

    if (source instanceof CommandSender cmdSource
        && BannedWords.checkAndWarn(cmdSource, event.getMessage())
    ) {
      event.setCancelled(true);
    }
  }
}