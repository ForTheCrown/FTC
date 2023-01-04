package net.forthecrown.events;

import static net.kyori.adventure.text.Component.text;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import net.forthecrown.core.Announcer;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class AnnouncementForwardingListener {
  @Subscribe
  public void onDiscordAnnounce(DiscordGuildMessageReceivedEvent event) {
    var rule = GeneralConfig.discordAnnouncementsToServer;
    var id = GeneralConfig.updateChannelId;

    if (id == 0 || !rule) {
      return;
    }

    if (event.getChannel() == null || event.getChannel().getIdLong() != id) {
      return;
    }

    var msg = event.getMessage();
    var jumpTo = msg.getJumpUrl();

    Announcer.get().announce(
        Text.format("New announcement in discord! {0}",
            NamedTextColor.YELLOW,

            text("[Click here to view]", NamedTextColor.AQUA)
                .clickEvent(ClickEvent.openUrl(jumpTo))
        )
    );
  }
}