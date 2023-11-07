package net.forthecrown.guilds.commands;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildMessages;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.user.User;

class GuildKickNode extends GuildCommandNode {

  GuildKickNode() {
    super("guildkick", "kick");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>", "Kicks a user out of your guild");

    factory.usage("<user> <guild>")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("Kicks a user out of a guild");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    var arg = argument("user", Arguments.USER);

    addGuildCommand(arg, (c, provider) -> {
      Guild guild = provider.get(c);
      User user = getUserSender(c);
      User target = Arguments.getUser(c, "user");

      testPermission(
          user,
          guild,
          GuildPermission.CAN_KICK,
          Exceptions.NO_PERMISSION
      );

      if (user.equals(target)) {
        throw GuildExceptions.KICK_SELF;
      }

      GuildMember member = guild.getMember(target.getUniqueId());

      if (member == null) {
        throw GuildExceptions.notGuildMember(target, guild);
      } else if (member.getRankId() == ID_LEADER) {
        throw GuildExceptions.CANNOT_KICK_LEADER;
      }

      guild.removeMember(target.getUniqueId());

      target.sendMessage(GuildMessages.guildKickedTarget(guild, user));
      guild.announce(GuildMessages.guildKickAnnouncement(user, target));

      if (!guild.isMember(user.getUniqueId())) {
        user.sendMessage(GuildMessages.guildKickedSender(guild, target));
      }

      return 0;
    });

    command.then(arg);
  }
}