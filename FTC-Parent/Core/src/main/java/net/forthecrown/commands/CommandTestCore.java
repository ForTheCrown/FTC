package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.FtcUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandTestCore extends FtcCommand {

    public CommandTestCore(){
        super("coretest", CrownCore.inst());

        setAliases("testcore");
        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    @Override
    public boolean test(CommandSource sender) { //test method used by Brigadier to determine who can use the command, from Predicate interface
        return sender.asBukkit().isOp() && testPermissionSilent(sender.asBukkit());
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser u = getUserSender(c);
            u.sendMessage("-Beginning test-");
            //Use this command to test things lol
            //This is as close as I currently know how to get to actual automatic test
            FtcUser user = (FtcUser) u;

            u.sendMessage("-Test finished-");
            return 0;
        });
    }
}
