package net.forthecrown.guilds.listeners;

import static net.kyori.adventure.text.Component.text;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.forthecrown.guilds.GuildDiscord;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.format.NamedTextColor;

public class GuildDiscordListener {
  @Subscribe
  public void onAccountLink(AccountLinkedEvent event) {
    User user = Users.get(event.getPlayer());
    var guild = Guilds.getGuild(user);

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
    var guild = Guilds.getGuild(user);

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

    for (var g: Guilds.getManager().getGuilds()) {
      var d = g.getDiscord();

      if (d.getChannelId() != event.getChannel().getIdLong()) {
        continue;
      }

      AccountLinkManager manager = DiscordSRV.getPlugin()
          .getAccountLinkManager();

      var id = manager.getUuid(event.getAuthor().getId());

      if (id == null) {
        g.sendMessage(
            Messages.chatMessage(
                text("[Discord] " + event.getAuthor().getEffectiveName()),
                text(event.getMessage().getContentDisplay())
            )
        );

        return;
      }

      User user = Users.get(id);

      g.sendMessage(
          text()
              .append(
                  text("[Discord] ", NamedTextColor.GRAY),

                  Messages.chatMessage(
                      user.displayName(),
                      Text.renderString(user, event.getMessage().getContentDisplay())
                  )
              )
              .build()
      );

      return;
    }
  }
}