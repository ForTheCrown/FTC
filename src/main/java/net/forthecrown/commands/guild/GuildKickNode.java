package net.forthecrown.commands.guild;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.user.User;

class GuildKickNode extends GuildCommandNode {

  GuildKickNode() {
    super("guildkick", "kick");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>", "Kicks a user out of your guild");

    factory.usage("<user> <guild>")
        .setPermission(Permissions.GUILD_ADMIN)
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
        throw Exceptions.KICK_SELF;
      }

      GuildMember member = guild.getMember(target.getUniqueId());

      if (member == null) {
        throw Exceptions.notGuildMember(target, guild);
      } else if (member.getRankId() == ID_LEADER) {
        throw Exceptions.CANNOT_KICK_LEADER;
      }

      guild.removeMember(target.getUniqueId());

      target.sendOrMail(Messages.guildKickedTarget(guild, user));
      guild.announce(Messages.guildKickAnnouncement(user, target));

      if (!guild.isMember(user.getUniqueId())) {
        user.sendMessage(Messages.guildKickedSender(guild, target));
      }

      return 0;
    });

    command.then(arg);
  }
}