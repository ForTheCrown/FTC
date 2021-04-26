package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.datafixers.UserAndBalanceUpdater;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.io.IOException;

public class CommandTestCore extends CrownCommandBuilder {

    public CommandTestCore(){
        super("coretest", FtcCore.getInstance());

        setAliases("testcore");
        setPermission("ftc.admin.test");
        register();
    }

    @Override
    public boolean test(CommandListenerWrapper sender) { //test method used by Brigadier to determine who can use the command, from Predicate interface
        return sender.getBukkitSender().isOp() && testPermissionSilent(sender.getBukkitSender());
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
                    CrownUser u = getUserSender(c);
                    u.sendMessage("-Beginning test-");
                    //Use this command to test things lol
                    //This is as close as I currently know how to get to actual automatic tests

                    try {
                        new UserAndBalanceUpdater(FtcCore.getInstance()).begin().complete();
                    } catch (IOException e){
                        e.printStackTrace();
                    }

                    u.sendMessage("do the data thing now");
                    u.sendMessage("-Test finished-");
                    return 0;
                });
    }
}
