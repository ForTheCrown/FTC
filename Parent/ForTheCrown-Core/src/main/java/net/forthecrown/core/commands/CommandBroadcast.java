package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;

public class CommandBroadcast extends CrownCommandBuilder {

    public CommandBroadcast(){
        super("broadcast", FtcCore.getInstance());

        setDescription("Broadcasts a message to the entire server.");
        setAliases("announce", "bc", "ac");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Broadcasts a message to the entire server
     *
     *
     * Valid usages of command:
     * - /broadcast
     * - /bc
     *
     * Permissions used:
     * - ftc.admin
     *
     * Author: Wout
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument("announcement", StringArgumentType.greedyString())
                .executes(context -> {
                    Announcer.prefixAc(context.getArgument("announcement", String.class));
                    return 0;
                })
        );
    }
}
