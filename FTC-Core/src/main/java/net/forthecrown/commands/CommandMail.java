package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.admin.CommandLore;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.user.actions.MailQuery;
import net.forthecrown.user.actions.UserActionHandler;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
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
                .executes(c -> readMail(c, 0))

                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(c -> {
                            int page = c.getArgument("page", Integer.class) - 1;
                            return readMail(c, page);
                        })
                )

                .then(literal("mark_unread")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    UserMail mail = user.getMail();
                                    int index = c.getArgument("index", Integer.class);

                                    if(!mail.isValidIndex(index)) {
                                        throw FtcExceptionProvider.translatable("mail.invalidIndex", Component.text(index));
                                    }

                                    UserMail.MailMessage message = mail.get(index);
                                    message.read = false;

                                    user.sendMessage(
                                            Component.translatable("mail.marked",
                                                    NamedTextColor.YELLOW,
                                                    Component.translatable("mail.unread")
                                            )
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("mark_read")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    UserMail mail = user.getMail();
                                    int index = c.getArgument("index", Integer.class);

                                    if(!mail.isValidIndex(index)) {
                                        throw FtcExceptionProvider.translatable("mail.invalidIndex", Component.text(index));
                                    }

                                    UserMail.MailMessage message = mail.get(index);
                                    message.read = true;

                                    user.sendMessage(
                                            Component.translatable("mail.marked",
                                                    NamedTextColor.YELLOW,
                                                    Component.translatable("mail.read")
                                            )
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("clear")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            user.getMail().clear();

                            user.sendMessage(
                                    Component.translatable("mail.cleared", NamedTextColor.YELLOW)
                            );
                            return 0;
                        })
                )

                .then(literal("send")
                        .then(CommandLore.compOrStringArg(
                                literal("all")
                                        .requires(source -> source.hasPermission(Permissions.ADMIN)),

                                (context, builder) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(context, builder, true),

                                (context, lore) -> {
                                    context.getSource().sendAdmin(
                                            Component.text("Sending all users mail: ")
                                                    .append(lore)
                                    );

                                    UserManager m = Crown.getUserManager();
                                    m.getAllUsers()
                                            .whenComplete((users, throwable) -> {
                                                if(throwable != null) {
                                                    context.getSource().sendAdmin("Error sending mail to all users, check console");
                                                    Crown.logger().error(throwable);

                                                    return;
                                                }

                                                users.forEach(user -> {
                                                    user.sendAndMail(lore, null);
                                                });

                                                m.unloadOffline();
                                                context.getSource().sendAdmin("Sent all users mail");
                                            });

                                    return 0;
                                }
                        ))

                        .then(argument("target", UserArgument.user())
                                .then(argument("message", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(context, builder, false))

                                        .executes(c -> {
                                            CrownUser user = getUserSender(c);
                                            CrownUser target = UserArgument.getUser(c, "target");

                                            String rawMessage = c.getArgument("message", String.class);
                                            Component message = FtcFormatter.formatIfAllowed(rawMessage, user);

                                            ActionFactory.addMail(target, message, user.getUniqueId());
                                            return 0;
                                        })
                                )
                        )
                );
    }

    private static int readMail(CommandContext<CommandSource> c, int page) throws CommandSyntaxException {
        MailQuery query = new MailQuery(c.getSource(), getUserSender(c), page);
        UserActionHandler.handleAction(query);
        return 1;
    }
}