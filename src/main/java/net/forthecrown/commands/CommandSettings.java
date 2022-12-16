package net.forthecrown.commands;

import net.forthecrown.core.SettingsBook;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;

public class CommandSettings extends FtcCommand {

    public CommandSettings() {
        super("settings");

        setAliases("options");
        setDescription("Displays a user's settings");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Shows some basic info about a user.
     *
     * Valid usages of command:
     * - /settings
     * - /options
     *
     * Author: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    SettingsBook.open(user);

                    return 0;
                });
    }
}