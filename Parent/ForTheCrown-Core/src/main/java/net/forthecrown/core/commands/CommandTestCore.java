package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandTestCore extends CrownCommandBuilder {

    public CommandTestCore(){
        super("coretest", FtcCore.getInstance());

        setAliases("testcore");
        setPermission("ftc.admin.test");
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
                    //This is as close as I currently know how to get to actual automatic tests

                    u.sendMessage("do the data thing now");
                    u.sendMessage("-Test finished-");
                    return 0;
                });
    }
}
