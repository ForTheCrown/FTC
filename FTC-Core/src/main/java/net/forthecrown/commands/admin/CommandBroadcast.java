package net.forthecrown.commands.admin;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandBroadcast extends FtcCommand {

    public CommandBroadcast(){
        super("broadcast", Crown.inst());

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
        CommandLore.addCompOrStringArg(command, (context, builder) -> builder.buildFuture(), (context, lore) -> {
            Crown.getAnnouncer().announce(lore);
            return 0;
        });
    }
}
