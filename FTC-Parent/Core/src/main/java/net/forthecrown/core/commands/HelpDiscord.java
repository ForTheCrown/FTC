package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class HelpDiscord extends FtcCommand {
    public HelpDiscord(){
        super("Discord", CrownCore.inst());

        setPermission(Permissions.HELP);
        setDescription("Gives you the servers discord link.");
        register();
    }

    /*
     * Sends the player the discord link
     */

    @Override
    protected void createCommand(BrigadierCommand command){
        command.executes(c ->{
            c.getSource().sendMessage(CrownCore.getPrefix() + CrownCore.getDiscord());
            return 0;
        });
    }
}
