package net.forthecrown.vikings.commands;

import net.forthecrown.emperor.commands.manager.FtcCommand;
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
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
    }
}