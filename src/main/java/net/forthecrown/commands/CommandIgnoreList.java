package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;

public class CommandIgnoreList extends FtcCommand {

    public CommandIgnoreList() {
        super("ignorelist");

        setPermission(Permissions.IGNORE);
        setDescription("Displays all the ignored players");
        setAliases(
                "blocked", "blockedplayers", "blockedlist",
                "ignoring", "ignored", "ignores",
                "ignoredlist", "ignoredplayers",
                "listignores", "listignored"
        );

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /<command> [user]
     *
     * Permissions used:
     * ftc.commands.ignore
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> displayIgnored(c.getSource(), getUserSender(c)))

                .then(argument("user", Arguments.USER)
                        .requires(s -> s.hasPermission(Permissions.IGNORELIST_OTHERS))

                        .executes(c -> displayIgnored(c.getSource(), Arguments.getUser(c, "user")))
                );
    }

    private int displayIgnored(CommandSource source, User user) throws CommandSyntaxException {
        UserInteractions interactions = user.getInteractions();

        if (interactions.getBlocked().isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        source.sendMessage(Messages.listBlocked(interactions.getBlocked()));
        return 0;
    }
}