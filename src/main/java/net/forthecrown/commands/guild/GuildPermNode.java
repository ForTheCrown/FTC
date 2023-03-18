package net.forthecrown.commands.guild;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildRank.ID_MEMBER;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildPermissionsBook;

class GuildPermNode extends GuildCommandNode {

  public GuildPermNode() {
    super("guildperm", "toggleperm");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .then(argument("permission", ArgumentTypes.enumType(GuildPermission.class))
            .then(argument("rank_id", IntegerArgumentType.integer(ID_MEMBER, ID_LEADER - 1))
                .then(guildArgument()
                    .executes(c -> {
                      var user = getUserSender(c);
                      var guild = providerForArgument().get(c);
                      var perm = c.getArgument("permission", GuildPermission.class);
                      var rankId = c.getArgument("rank_id", Integer.class);

                      testPermission(
                          user,
                          guild,
                          GuildPermission.CAN_CHANGE_RANKS,
                          Exceptions.NO_PERMISSION
                      );

                      var rank = guild.getSettings()
                          .getRank(rankId);

                      if (rank == null) {
                        throw Exceptions.notARank(rankId);
                      }

                      boolean state = rank.togglePermission(perm);

                      user.sendMessage(
                          Messages.toggleMessage(perm.getMessageFormat(), state)
                      );

                      GuildPermissionsBook.open(
                          user, guild, rank
                      );

                      return 0;
                    })
                )
            )
        );
  }
}