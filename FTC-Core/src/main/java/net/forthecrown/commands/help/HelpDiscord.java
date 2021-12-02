package net.forthecrown.commands.help;

import github.scarsz.discordsrv.commands.CommandLink;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;

public class HelpDiscord extends FtcCommand {
    public HelpDiscord(){
        super("Discord", Crown.inst());

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
            c.getSource().sendMessage(
                    Component.text()
                            .append(Crown.prefix())
                            .append(Component.text(Crown.getDiscord()))
                            .build()
            );
            return 0;
        })
                .then(literal("link")
                        .executes(c -> {
                            CommandLink.execute(c.getSource().asPlayer(), new String[0]);
                            return 0;
                        })
                );
    }
}
