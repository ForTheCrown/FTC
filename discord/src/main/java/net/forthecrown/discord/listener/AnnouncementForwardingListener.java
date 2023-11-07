package net.forthecrown.discord.listener;

import static net.kyori.adventure.text.Component.text;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import net.forthecrown.discord.DiscordPlugin;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageRenderer;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class AnnouncementForwardingListener {

  private final DiscordPlugin plugin;

  public AnnouncementForwardingListener(DiscordPlugin plugin) {
    this.plugin = plugin;
  }

  @Subscribe
  public void onDiscordAnnounce(DiscordGuildMessageReceivedEvent event) {
    var config = plugin.getPluginConfig();

    var rule = config.forwardDiscordAnnouncementsToServer();
    var id = config.updateChannelId();

    if (id == 0 || !rule) {
      return;
    }

    if (event.getChannel() == null
        || event.getChannel().getIdLong() != id
        || event.getAuthor().isBot()
        || event.getAuthor().isSystem()
    ) {
      return;
    }

    var msg = event.getMessage();
    var jumpTo = msg.getJumpUrl();

    var text = Text.format("New announcement in discord! {0}",
        NamedTextColor.YELLOW,

        text("[Click here to view]", NamedTextColor.AQUA)
            .clickEvent(ClickEvent.openUrl(jumpTo))
            .hoverEvent(Messages.CLICK_ME)
    );

    ChannelledMessage.create(text)
        .setBroadcast()
        .setRenderer(MessageRenderer.FTC_PREFIX)
        .send();
  }
}