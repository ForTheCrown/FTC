package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

class GuildChatNode extends GuildCommandNode {
    GuildChatNode() {
        super("guildchat", "chat", "c");
        setAliases("gc");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("chat <message>", "Sends a chat into the guild chat");

        if (source.hasPermission(Permissions.GUILD_ADMIN)) {
            writer.field("chat <guild> <message>", "Sends a message into a guild's chat");
        }
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        command
                .then(argument("message", Arguments.MESSAGE)
                        .executes(c -> chat(c, GuildProvider.SENDERS_GUILD))
                )

                .then(guildArgument()
                        .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

                        .then(argument("message", Arguments.MESSAGE)
                                .executes(c -> chat(c, providerForArgument()))
                        )
                );
    }

    private int chat(CommandContext<CommandSource> c,
                     GuildProvider provider
    ) throws CommandSyntaxException {
        User user = getUserSender(c);
        Guild guild = provider.get(c);
        Component message = Arguments.getMessage(c, "message");

        var mute = Punishments.checkMute(user);

        if (BannedWords.checkAndWarn(user.getPlayer(), message)) {
            mute = Mute.HARD;
        }

        EavesDropper.reportGuildChat(user, mute, guild, message);

        if (!mute.isVisibleToOthers()) {
            return 0;
        }

        Mute finalMute = mute;
        guild.getMembers()
                .values()
                .stream()
                .filter(member -> {
                    if (finalMute == Mute.SOFT) {
                        return member.getId().equals(user.getUniqueId());
                    }

                    var viewer = member.getUser();

                    if (!viewer.isOnline()) {
                        viewer.unloadIfOffline();
                        return false;
                    }

                    return !Users.areBlocked(user, viewer);
                })

                .forEach(member -> {
                    var viewer = member.getUser();
                    boolean showRank = viewer.get(Properties.GUILD_RANKED_TAGS);
                    var rank = guild.getSettings().getRank(member.getRankId());

                    Component displayName = Users.createListName(
                            user,
                            text()
                                    .append(showRank ?
                                            rank.getFormattedName().append(space())
                                            : Component.empty()
                                    )
                                    .append(user.getTabName())
                                    .build(),
                            false
                    );

                    viewer.sendMessage(
                            Messages.guildChat(guild, displayName, message)
                    );
                });

        return 0;
    }
}