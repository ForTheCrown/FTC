package net.forthecrown.vikings.commands;

import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.vikings.VikingPerms;
import net.forthecrown.vikings.Vikings;

public class CommandVRaid extends FtcCommand {

    public CommandVRaid() {
        super("vraid", Vikings.inst);

        setPermission(VikingPerms.VIKING_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * - Too many to list lol
     *
     * Permissions used:
     * ftc.vikings.admin
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("key",))
                )
    }
}