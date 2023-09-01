package net.forthecrown.guilds.commands;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.GuildMessages;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildPermissions;

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
        .setPermission(GuildPermissions.GUILD_ADMIN)
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
      throw GuildExceptions.notGuildMember(user, guild);
    }

    if (member.getRankId() == ID_LEADER) {
      throw promotes
          ? GuildExceptions.PROMOTE_LEADER
          : GuildExceptions.DEMOTE_LEADER;
    }

    if (self) {
      throw promotes
          ? GuildExceptions.PROMOTE_SELF
          : GuildExceptions.DEMOTE_SELF;
    }

    var opt = promotes ? member.promote() : member.demote();

    if (opt.isPresent()) {
      throw opt.get();
    }

    var rankId = member.getRankId();
    var rank = guild.getSettings().getRank(rankId);

    guild.announce(
        GuildMessages.rankChangeAnnouncement(promotes, sender, user, rank)
    );

    if (!guild.isMember(user.getUniqueId())) {
      sender.sendMessage(GuildMessages.changedRank(promotes, user, rank, guild));
    }

    return 0;
  }
}