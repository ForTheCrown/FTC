package net.forthecrown.commands;

import com.mojang.brigadier.Command;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ProfilePrinter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;

public class CommandProfile extends FtcCommand {

    public CommandProfile(){
        super("profile", Crown.inst());

        setAliases("user", "playerprofile", "gameprofile");
        setDescription("Displays a user's information");
        setPermission(Permissions.PROFILE);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Shows some basic info about a user.
     *
     * Valid usages of command:
     * - /profile
     * - /profile [player]
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(command(false))
                .then(argument("player", UserArgument.user())
                        .executes(command(true))
                );
    }

    public Command<CommandSource> command(boolean userGiven) {
        return c -> {
            CommandSource s = c.getSource();
            CrownUser user = userGiven ? UserArgument.getUser(c, "player") : getUserSender(c);
            ProfilePrinter printer = new ProfilePrinter(user, s);

            if(!printer.isViewingAllowed()) {
                throw FtcExceptionProvider.translatable("commands.profileNotPublic", user.nickDisplayName());
            }

            s.sendMessage(printer.printFull());
            return 0;
        };
    }
}
