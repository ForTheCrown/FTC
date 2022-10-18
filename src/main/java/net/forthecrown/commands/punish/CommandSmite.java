package net.forthecrown.commands.punish;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;

public class CommandSmite extends FtcCommand {

    public CommandSmite() {
        super("smite");

        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Smites a player with lightning
     *
     * Valid usages of command:
     * /smite <player>
     *
     * Permissions used:
     * ftc.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.ONLINE_USER)
                        .executes(c -> {
                            User user = Arguments.getUser(c, "user");

                            user.getWorld().strikeLightning(user.getLocation());

                            c.getSource().sendMessage(
                                    Text.format("Smiting {0, user}.", user)
                            );
                            return 0;
                        })
                );
    }
}