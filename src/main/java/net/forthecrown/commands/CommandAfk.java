package net.forthecrown.commands;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

public class CommandAfk extends FtcCommand {
    public CommandAfk() {
        super("afk");

        setDescription("Marks or un-marks you as AFK");
        setPermission(Permissions.DEFAULT);
        setAliases("away");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> afk(getUserSender(c), null))

                .then(literal("-other")
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .then(argument("user", Arguments.ONLINE_USER)
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .executes(c -> afk(
                                        Arguments.getUser(c, "user"),
                                        null
                                ))
                        )
                )

                .then(argument("msg", Arguments.MESSAGE)
                        .executes(c -> afk(
                                getUserSender(c),
                                Arguments.getMessage(c, "msg")
                        ))
                );
    }

    private int afk(User user, Component message) {
        boolean alreadyAFK = user.isAfk();

        if (alreadyAFK) {
            user.unafk();
        } else {
            user.afk(message);
        }

        return 0;
    }
}