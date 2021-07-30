package net.forthecrown.commands.help;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class HelpDiscord extends FtcCommand {
    public HelpDiscord(){
        super("Discord", ForTheCrown.inst());

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
            c.getSource().sendMessage(ForTheCrown.getPrefix() + ForTheCrown.getDiscord());
            return 0;
        });
    }
}
