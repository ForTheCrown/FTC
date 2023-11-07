package net.forthecrown.antigrief.listeners;

import static net.forthecrown.antigrief.StaffChat.COOL_CLUB;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import net.forthecrown.antigrief.StaffChat;
import net.forthecrown.discord.FtcDiscord;
import net.forthecrown.text.PlayerMessage;

class StaffChatDiscordListener {

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

    var coolClub = FtcDiscord.findChannel(COOL_CLUB);

    if (coolClub.isEmpty() || !coolClub.get().equals(channel)) {
      return;
    }

    StaffChat.newMessage()
        .setSource(event.getMember())
        .setLogged(false)
        .setFromDiscord(true)
        .setMessage(PlayerMessage.allFlags(event.getMessage().getContentDisplay()))
        .send();
  }
}
