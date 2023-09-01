package net.forthecrown.guilds.commands;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildMessages;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.user.User;

class GuildDeleteNode extends GuildCommandNode {

  GuildDeleteNode() {
    super("deleteguild", "delete");
    setAliases("guilddelete", "gdelete");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Deletes your guild");

    factory.usage("<guild>")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("Deletes a guild");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    addGuildCommand(command, (c, provider) -> {
      Guild guild = provider.get(c);
      User user = getUserSender(c);

      GuildMember member = guild.getMember(user.getUniqueId());
      String reason;

      if (member == null) {
        if (!user.hasPermission(GuildPermissions.GUILD_ADMIN)) {
          throw Exceptions.NO_PERMISSION;
        }

        reason = "Closed by admin";
      } else {
        if (member.getRankId() != ID_LEADER) {
          throw Exceptions.NO_PERMISSION;
        }

        reason = "Closed by owner";
      }

      guild.announce(GuildMessages.guildDeletedAnnouncement(user));
      Guilds.removeAndArchive(guild, user.getName(), reason);

      if (!guild.isMember(user.getUniqueId())) {
        user.sendMessage(GuildMessages.guildDeleted(guild));
      }

      return 0;
    });
  }
}