package net.forthecrown.commands.user;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.permissions.Permission;

public class UserCommands extends CmdUtil {
    static final Permission PERMISSION = Permissions.ADMIN;
    static final String USER_ARG_NAME = "user";

    static final UserCommandNode[] NODES = {
            new UserTimeNode(),
            new UserTitlesNode(),
            new UserCosmeticsNode(),
            new UserEarningsNode(),
            new UserTabNode()
    };

    public static void createCommands() {
        new UserCommand().register();
    }

    static class UserCommand extends FtcCommand {
        public UserCommand() {
            super("ftcuser");

            setAliases("users", "user");
            setPermission(PERMISSION);
        }

        @Override
        protected void createCommand(BrigadierCommand command) {
            var argument = argument(USER_ARG_NAME, Arguments.USER);
            UserProvider provider = c -> Arguments.getUser(c, USER_ARG_NAME);

            for (var n: NODES) {
                n.register();

                var literal = literal(n.argumentName);
                n.create(literal, provider);

                argument.then(literal);
            }

            command.then(argument);
        }
    }
}