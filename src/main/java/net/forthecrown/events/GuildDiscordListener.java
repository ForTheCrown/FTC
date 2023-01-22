package net.forthecrown.events;

import static net.kyori.adventure.text.Component.text;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.forthecrown.core.Messages;
import net.forthecrown.guilds.GuildDiscord;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.ChatParser;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.Listener;

public class GuildDiscordListener implements Listener {
  @Subscribe
  public void onAccountLink(AccountLinkedEvent event) {
    User user = Users.get(event.getPlayer());
    var guild = user.getGuild();

    if (guild == null) {
      return;
    }

    var disc = guild.getDiscord();
    var member = DiscordUtil.getMemberById(event.getUser().getId());

    if (member == null) {
      return;
    }

    disc.getRole().ifPresent(role -> {
      role.getGuild()
          .addRoleToMember(member, role)
          .submit();
    });

    disc.getChannel().ifPresent(channel -> {
      channel
          .putPermissionOverride(member)
          .setAllow(GuildDiscord.memberOverridePerms())
          .submit();
    });
  }

  @Subscribe
  public void onAccountUnlink(AccountUnlinkedEvent event) {
    var user = Users.get(event.getPlayer());
    var guild = user.getGuild();

    if (guild == null) {
      return;
    }

    var dis = guild.getDiscord();
    var member = DiscordUtil.getMemberById(event.getDiscordId());

    if (member == null) {
      return;
    }

    dis.getChannel().ifPresent(channel -> {
      channel.getManager()
          .removePermissionOverride(member)
          .submit();
    });

    dis.getRole().ifPresent(role -> {
      role.getGuild()
          .removeRoleFromMember(member, role)
          .submit();
    });
  }

  @Subscribe
  public void onMessageReceive(DiscordGuildMessageReceivedEvent event) {
    if (event.getChannel() == null
        || event.getAuthor().isBot()
        || event.getAuthor().isSystem()
    ) {
      return;
    }

    for (var g: GuildManager.get().getGuilds()) {
      var d = g.getDiscord();

      if (d.getChannelId() != event.getChannel().getIdLong()) {
        continue;
      }

      AccountLinkManager manager = DiscordSRV.getPlugin()
          .getAccountLinkManager();

      var id = manager.getUuid(event.getAuthor().getId());

      if (id == null) {
        g.sendMessage(
            text(event.getMessage().getContentDisplay())
        );
        return;
      }

      User user = Users.get(id);

      g.sendMessage(
          text()
              .append(
                  g.getPrefix(),

                  text("[Discord] ", NamedTextColor.GRAY),

                  Messages.chatMessage(
                      user.displayName(),

                      ChatParser.of(user)
                          .render(event.getMessage().getContentDisplay())
                  )
              )
              .build()
      );

      return;
    }
  }
}