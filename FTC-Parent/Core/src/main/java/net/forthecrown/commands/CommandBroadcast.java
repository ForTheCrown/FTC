package net.forthecrown.commands;

import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandBroadcast extends FtcCommand {

    public CommandBroadcast(){
        super("broadcast", CrownCore.inst());

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
                .then(CommandLore.compOrStringArg(literal("ac"), (c, b) -> Suggestions.empty(), ((context, lore) -> {
                    CrownCore.getAnnouncer().announce(lore);
                    return 0;
                })));
    }
}
