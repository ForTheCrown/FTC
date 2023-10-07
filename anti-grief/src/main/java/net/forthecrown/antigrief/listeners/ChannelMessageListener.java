package net.forthecrown.antigrief.listeners;

import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.antigrief.Mute;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.events.ChannelMessageEvent;
import net.forthecrown.text.channel.ChannelMessageState;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

class ChannelMessageListener implements Listener {

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onChannelMessage(ChannelMessageEvent event) {
    Audience source = event.getUserSource();

    if (source == null || event.isAnnouncement()) {
      return;
    }

    Mute mute = Punishments.checkMute(source);

    if (mute == Mute.HARD) {
      event.setState(ChannelMessageState.CANCELLED);
      return;
    }

    if (mute == Mute.SOFT) {
      event.setState(ChannelMessageState.SOFT_CANCELLED);
    }

    if (source instanceof CommandSender cmdSource
        && BannedWords.checkAndWarn(cmdSource, event.getMessage())
    ) {
      event.setState(ChannelMessageState.CANCELLED);
    }
  }
}