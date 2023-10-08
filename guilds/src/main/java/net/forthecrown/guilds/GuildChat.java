package net.forthecrown.guilds;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;

import net.forthecrown.text.Messages;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.channel.ChannelMessageState;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageHandler;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
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
      Component rankPrefix = getRankTag(user, viewer);

      return Component.textOfChildren(
          rankPrefix,
          displayName,
          space(),
          Messages.chatMessage(senderName, baseMessage)
      );
    });

    chn.setHandler(MessageHandler.DEFAULT.postHandle(event -> {
      if (event.getState() != ChannelMessageState.FINE) {
        return;
      }

      guild.getDiscord().forwardGuildChat(user, event.getMessage().create(null));
    }));

    chn.send();
  }

  static Component getRankTag(User user, Audience viewer) {
    boolean showRankTags = Properties.getValue(viewer, GUserProperties.GUILD_RANKED_TAGS);

    if (!showRankTags) {
      return empty();
    }

    var userGuild = Guilds.getGuild(user);

    if (userGuild == null) {
      return empty();
    }

    GuildMember member = userGuild.getMember(user.getUniqueId());
    GuildRank rank = userGuild.getSettings().getRank(member.getRankId());

    return rank.getFormattedName().append(space());
  }
}
