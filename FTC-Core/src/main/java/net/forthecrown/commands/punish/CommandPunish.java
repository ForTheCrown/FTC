package net.forthecrown.commands.punish;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.admin.gui.AdminGUI;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.user.CrownUser;

public class CommandPunish extends FtcCommand {

    public CommandPunish() {
        super("Punish");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Punish
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserArgument.getUser(c, "user");

                            BuiltInventory inventory = AdminGUI.createOveriew(target);
                            inventory.open(user);

                            return 0;
                        })
                );
    }
}