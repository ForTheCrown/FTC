package net.forthecrown.commands.guild;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildPermission;

class GuildChangeRankNode extends GuildCommandNode {

  private final boolean promotes;

  public GuildChangeRankNode(String cmd, String argument, boolean promotes) {
    super(cmd, argument);
    this.promotes = promotes;
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>")
        .addInfo((promotes ? "Promotes" : "Demotes") + " a user");

    factory.usage("<user> <guild>")
        .setPermission(Permissions.GUILD_ADMIN)
        .addInfo((promotes ? "Promotes" : "Demotes") + " a user in a guild");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    var userArg = argument("user", Arguments.USER);
    addGuildCommand(userArg, this::changeRank);
    command.then(userArg);
  }

  private int changeRank(CommandContext<CommandSource> c,
                         GuildProvider provider
  ) throws CommandSyntaxException {
    var sender = getUserSender(c);
    var user = Arguments.getUser(c, "user");
    var guild = provider.get(c);

    testPermission(sender, guild, GuildPermission.CAN_RERANK, Exceptions.NO_PERMISSION);

    var member = guild.getMember(user.getUniqueId());
    boolean self = user.equals(sender);

    if (member == null || member.hasLeft()) {
      throw Exceptions.notGuildMember(user, guild);
    }

    if (member.getRankId() == ID_LEADER) {
      throw promotes
          ? Exceptions.PROMOTE_LEADER
          : Exceptions.DEMOTE_LEADER;
    }

    if (self) {
      throw promotes
          ? Exceptions.PROMOTE_SELF
          : Exceptions.DEMOTE_SELF;
    }

    var opt = promotes ? member.promote() : member.demote();

    if (opt.isPresent()) {
      throw opt.get();
    }

    var rankId = member.getRankId();
    var rank = guild.getSettings().getRank(rankId);

    guild.announce(
        Messages.rankChangeAnnouncement(promotes, sender, user, rank)
    );

    if (!guild.isMember(user.getUniqueId())) {
      sender.sendMessage(Messages.changedRank(promotes, user, rank, guild));
    }

    return 0;
  }
}