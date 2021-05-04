package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class HelpDiscord extends CrownCommandBuilder {
    public HelpDiscord(){
        super("Discord", FtcCore.getInstance());

        setPermission(null);
        setDescription("Gives you the servers discord link.");
        register();
    }

    /*
     * Sends the player the discord link
     */

    @Override
    protected void createCommand(BrigadierCommand command){
        command.executes(c ->{
            c.getSource().sendMessage(FtcCore.getPrefix() + FtcCore.getDiscord());
            return 0;
        });
    }
}
