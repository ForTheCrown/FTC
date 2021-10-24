package net.forthecrown.commands.mail;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.actions.ActionFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMail extends FtcCommand {
    public static final int ENTRIES_PER_PAGE = 5;

    public CommandMail() {
        super("mail");

        setPermission(Permissions.MAIL);
        setDescription("Does mail stuff, idk");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Mail
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> readMail(c, false, true))

                .then(literal("read_other")
                        .requires(s -> s.hasPermission(Permissions.MAIL_OTHERS))

                        .then(argument("user", UserArgument.user())
                                .executes(c -> readMail(c, false, false))

                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .executes(c -> readMail(c, true, false))
                                )
                        )
                )

                .then(literal("read")
                        .executes(c -> readMail(c, false, true))

                        .then(argument("page", IntegerArgumentType.integer(1))
                                .executes(c -> readMail(c, true, true))
                        )
                )

                .then(literal("send")
                        .then(argument("target", UserArgument.users())
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            CrownUser user = getUserSender(c);
                                            CrownUser target = UserArgument.getUser(c, "target");

                                            Component message = FtcFormatter.formatIfAllowed(
                                                    c.getArgument("message", String.class),
                                                    user
                                            );

                                            ActionFactory.addMail(user, message, target.getUniqueId());
                                            return 0;
                                        })
                                )
                        )
                );
    }

    private int readMail(CommandContext<CommandSource> context, boolean pageGiven, boolean userIsViewer) throws CommandSyntaxException {
        MailReadContext c = new MailReadContext(context, pageGiven, userIsViewer);
        TextComponent.Builder builder = Component.text()
                .append(
                        c.user.nickDisplayName()
                                .color(NamedTextColor.YELLOW)
                                .append(Component.text("'s mail"))
                );

        c.viewer.sendMessage(builder.build());
        return 0;
    }

    private class MailReadContext {
        final int page;
        final int firstIndex;
        final boolean userIsViewing;

        final CommandSource viewer;

        final CrownUser user;
        final UserMail mail;

        MailReadContext(CommandContext<CommandSource> c, boolean pageGiven, boolean userIsViewer) throws CommandSyntaxException {
            page = pageGiven ? c.getArgument("page", Integer.class)-1 : 0;
            firstIndex = page * ENTRIES_PER_PAGE;

            this.viewer = c.getSource();
            this.user = userIsViewer ? getUserSender(c) : UserArgument.getUser(c, "user");
            this.mail = user.getMail();
            this.userIsViewing = userIsViewer;
        }
    }
}