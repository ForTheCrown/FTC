package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class HelpDiscord extends CrownCommandBuilder {
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
