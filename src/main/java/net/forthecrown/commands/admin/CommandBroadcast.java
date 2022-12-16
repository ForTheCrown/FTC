package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Announcer;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;

public class CommandBroadcast extends FtcCommand {

    public CommandBroadcast(){
        super("broadcast");

        setDescription("Broadcasts a message to the entire server.");
        setAliases("announce", "bc", "ac");
        setPermission(Permissions.BROADCAST);
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
     * - ftc.commands.broadcast
     *
     * Author: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("message", Arguments.CHAT)
                        .executes(c -> {
                            Announcer.get().announce(c.getArgument("message", Component.class));
                            return 0;
                        })
                );
    }
}