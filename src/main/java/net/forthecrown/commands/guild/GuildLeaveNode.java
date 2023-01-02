package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guilds;

class GuildLeaveNode extends GuildCommandNode {

  public GuildLeaveNode() {
    super("guildleave", "leave");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Leaves the current guild you're in");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .executes(c -> {
          var user = getUserSender(c);
          var guild = GuildProvider.SENDERS_GUILD.get(c);

          if (guild.getMemberSize() == 1) {
            guild.removeMember(user.getUniqueId());

            Guilds.removeAndArchive(
                guild, user.getName(), "No members left"
            );

            user.sendMessage(Messages.leftGuild(guild));
            user.sendMessage(Messages.GUILD_DELETED_EMPTY);
          } else {
            if (guild.isLeader(user)) {
              throw Exceptions.GLEADER_CANNOT_LEAVE;
            }

            user.sendMessage(Messages.leftGuild(guild));
            guild.removeMember(user.getUniqueId());
            guild.announce(Messages.leftGuildAnnouncement(user));
          }

          return 0;
        });
  }
}