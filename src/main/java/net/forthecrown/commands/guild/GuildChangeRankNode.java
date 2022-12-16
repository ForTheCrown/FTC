package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildRank;
import net.forthecrown.utils.text.writer.TextWriter;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildRank.ID_MEMBER;

class GuildChangeRankNode extends GuildCommandNode {
    private final boolean promotes;

    public GuildChangeRankNode(String cmd, String argument, boolean promotes) {
        super(cmd, argument);
        this.promotes = promotes;
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field((getArgumentName()[0]) + " <user>", "Demotes a user");

        if (source.hasPermission(Permissions.GUILD_ADMIN)) {
            writer.field(
                    (getArgumentName()[0]) +  " <user> <guild>",
                    "Demotes a user in the given guild"
            );
        }
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
            throw promotes ? Exceptions.PROMOTE_LEADER : Exceptions.DEMOTE_LEADER;
        }

        if (self) {
            throw promotes ? Exceptions.PROMOTE_SELF : Exceptions.DEMOTE_SELF;
        }

        int nextId = member.getRankId();
        GuildRank rank = null;

        // While the Next rank's ID is in the valid bounds, shift
        // the ID either up or down, depending on if we're promoting
        // or demoting
        while ((nextId += (promotes ? 1 : -1)) >= ID_MEMBER
                && nextId < ID_LEADER
        ) {
            if (guild.getSettings().hasRank(nextId)) {
                rank = guild.getSettings().getRank(nextId);
                break;
            }
        }

        if (rank == null) {
            throw promotes ? Exceptions.cannotPromote(user) : Exceptions.cannotDemote(user);
        }

        member.setRankId(rank.getId());

        if (!guild.isMember(user.getUniqueId())) {
            sender.sendMessage(Messages.changedRank(promotes, user, rank, guild));
        }

        guild.sendMessage(
                Messages.rankChangeAnnouncement(promotes, sender, user, rank)
        );

        return 0;
    }
}