package net.forthecrown.commands.guild;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.guilds.menu.GuildMenus;

public class GuildCommands {
    static final GuildCommandNode[] NODES = {
            new GuildHelpNode(),
            new GuildInfoNode(),
            new GuildListNode(),

            new GuildSetNode(),
            new GuildPermNode(),

            new GuildInviteNode(),
            new GuildKickNode(),
            new GuildJoinNode(),
            new GuildLeaveNode(),

            new GuildChunkNode(),

            new GuildChatNode(),

            new GuildCreateNode(),
            new GuildDeleteNode(),

            new GuildDiscoveryNode(),

            new GuildChangeRankNode("guildpromote", "promote", true),
            new GuildChangeRankNode("guilddemote", "demote", false),
    };

    public static void createCommands() {
        new CommandGuild();
    }

    static class CommandGuild extends FtcCommand {
        public CommandGuild() {
            super("guild");

            setPermission(Permissions.GUILD);
            setDescription("Guild command");
            setAliases("g");

            register();
        }

        @Override
        protected void createCommand(BrigadierCommand command) {
            for (var n: NODES) {
                // The help command shouldn't be created
                if (!n.getName().contains("help")) {
                    n.register();
                }

                for (var name: n.getArgumentName()) {
                    var literal = literal(name)
                            .executes(c -> {
                                StringReader reader = new StringReader(c.getInput());
                                reader.setCursor(reader.getTotalLength());

                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                                        .dispatcherUnknownArgument()
                                        .createWithContext(reader);
                            })

                            .requires(n);

                    n.create(literal);
                    command.then(literal);
                }
            }

            command
                    .executes(context -> {
                        var user = getUserSender(context);

                        if (user.getGuild() == null) {
                            GuildMenus.open(
                                    GuildMenus.DISCOVERY_MENU,
                                    user,
                                    null
                            );
                            return 0;
                        }

                        GuildMenus.open(
                                GuildMenus.MAIN_MENU,
                                user,
                                user.getGuild()
                        );
                        return 0;
                    });
        }
    }
}