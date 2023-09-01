package net.forthecrown.guilds;

import static net.kyori.adventure.text.Component.space;

import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageHandler;
import net.forthecrown.text.Messages;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

public class GuildChat {

  public static void send(Guild guild, User user, PlayerMessage message) {
    var chn = ChannelledMessage.create(message);
    chn.setSource(user);
    chn.shownToSource(true);
    chn.addTargets(guild.getOnlineMembers());
    chn.setChannelName("guild_chat/" + guild.getId());

    chn.setRenderer((viewer, baseMessage) -> {
      Component displayName = guild.displayName();
      Component senderName  = user.displayName(viewer);

      return Component.textOfChildren(
          displayName,
          space(),
          Messages.chatMessage(senderName, baseMessage)
      );
    });

    chn.setHandler(MessageHandler.DEFAULT.postHandle(event -> {
      if (event.isCancelled() || event.getTargets().isEmpty()) {
        return;
      }

      guild.getDiscord().forwardGuildChat(user, message.create(null));
    }));

    chn.send();
  }

}
