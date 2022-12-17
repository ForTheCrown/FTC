package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.writer.TextWriter;

class GuildJoinNode extends GuildCommandNode {

  GuildJoinNode() {
    super("guildjoin", "join");
  }

  @Override
  protected void writeHelpInfo(TextWriter writer, CommandSource source) {
    writer.field("join <guild>", "Joins a guild");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .then(guildArgument()
            .executes(c -> {
              User user = getUserSender(c);
              Guild guild = providerForArgument().get(c);

              GuildInviteNode.ensureJoinable(user, guild);

              if (!guild.getSettings().isPublic()) {
                return GuildInviteNode.acceptInvite(c);
              }

              guild.join(user);
              return 0;
            })
        );
  }
}