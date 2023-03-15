package net.forthecrown.events;

import static net.forthecrown.core.FtcDiscord.COOL_CLUB;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.logging.Loggers;
import net.kyori.adventure.text.Component;

public class DiscordStaffChatListener {

  @Subscribe
  public void onMessage(DiscordGuildMessageReceivedEvent event) {
    var channel = event.getChannel();
    var author = event.getAuthor();

    if (author.isBot()
        || author.isSystem()
        || event.getMessage().isWebhookMessage()
    ) {
      return;
    }

    var coolClub = DiscordSRV.getPlugin()
        .getDestinationTextChannelForGameChannelName(COOL_CLUB);

    if (coolClub == null || !coolClub.equals(channel)) {
      return;
    }

    Loggers.getLogger()
        .debug("message.raw='{}'", event.getMessage().getContentRaw());

    Loggers.getLogger()
        .debug("message.flags={}", event.getMessage().getFlags());

    StaffChat.newMessage()
        .setSource(event.getMember())
        .setLogged(false)
        .setFromDiscord(true)
        .setMessage(Component.text(event.getMessage().getContentDisplay()))
        .send();
  }
}