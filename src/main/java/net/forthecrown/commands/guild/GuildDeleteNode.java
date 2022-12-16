package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.writer.TextWriter;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;

class GuildDeleteNode extends GuildCommandNode {
    GuildDeleteNode() {
        super("deleteguild", "delete");
        setAliases("guilddelete");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("delete", "Deletes your guild");

        if (source.hasPermission(Permissions.GUILD_ADMIN)) {
            writer.field("delete <guild>", "Deletes a guild");
        }
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        addGuildCommand(command, (c, provider) -> {
            Guild guild = provider.get(c);
            User user = getUserSender(c);

            GuildMember member = guild.getMember(user.getUniqueId());
            String reason;

            if (member == null) {
                if (!user.hasPermission(Permissions.GUILD_ADMIN)) {
                    throw Exceptions.NO_PERMISSION;
                }

                reason = "Closed by admin";
            } else {
                if (member.getRankId() != ID_LEADER) {
                    throw Exceptions.NO_PERMISSION;
                }

                reason = "Closed by owner";
            }

            Guilds.removeAndArchive(guild, user.getName(), reason);

            guild.sendMessage(Messages.guildDeletedAnnouncement(user));

            if (!guild.isMember(user.getUniqueId())) {
                user.sendMessage(Messages.guildDeleted(guild));
            }

            return 0;
        });
    }
}