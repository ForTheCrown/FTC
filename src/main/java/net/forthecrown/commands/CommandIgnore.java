package net.forthecrown.commands;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;

public class CommandIgnore extends FtcCommand {
    public CommandIgnore(){
        super("ignore");

        setPermission(Permissions.IGNORE);
        setAliases("ignoreplayer", "unignore", "unignoreplayer", "block", "unblock");
        setDescription("Makes you ignore/unignore another player");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.USER)
                        .executes(c -> {
                            User user = getUserSender(c);
                            User target = Arguments.getUser(c, "user");

                            if (target.equals(user)) {
                                throw Exceptions.CANNOT_IGNORE_SELF;
                            }

                            UserInteractions userInt = user.getInteractions();
                            boolean alreadyIgnoring = userInt.isOnlyBlocked(target.getUniqueId());

                            if (alreadyIgnoring) {
                                user.sendMessage(Messages.unignorePlayer(target));
                                userInt.removeBlocked(target.getUniqueId());
                            } else {
                                user.sendMessage(Messages.ignorePlayer(target));
                                userInt.addBlocked(target.getUniqueId());
                            }

                            return 0;
                        })
                );
    }
}