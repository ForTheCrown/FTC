package net.forthecrown.guilds.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.GuildMessages;
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

            user.sendMessage(GuildMessages.leftGuild(guild));
            user.sendMessage(GuildMessages.GUILD_DELETED_EMPTY);
          } else {
            if (guild.isLeader(user)) {
              throw GuildExceptions.GLEADER_CANNOT_LEAVE;
            }

            user.sendMessage(GuildMessages.leftGuild(guild));
            guild.removeMember(user.getUniqueId());
            guild.announce(GuildMessages.leftGuildAnnouncement(user));
          }

          return 0;
        });
  }
}