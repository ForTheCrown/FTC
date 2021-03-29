package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;

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
    protected void registerCommand(BrigadierCommand command){
        command.executes(c ->{
            c.getSource().getBukkitSender().sendMessage(FtcCore.getPrefix() + FtcCore.getDiscord());
            return 0;
        });
    }
}
