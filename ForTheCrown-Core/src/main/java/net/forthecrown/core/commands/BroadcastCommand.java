package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BroadcastCommand extends CrownCommand  {

    public BroadcastCommand(){
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
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix
     *
     * Author: Wout
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) throws CrownException {
        if(args.length < 1) return false;

        FtcCore.getAnnouncer().announceToAll(String.join(" ", args));
        return true;
    }
}
