package net.forthecrown.commands.help;

import github.scarsz.discordsrv.commands.CommandLink;
import github.scarsz.discordsrv.commands.CommandUnlink;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

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
                            .append(Component.text("Join our discord: "))
                            .append(
                                    Component.text(FtcVars.discordLink.get())
                                            .color(NamedTextColor.AQUA)
                                            .clickEvent(ClickEvent.openUrl(FtcVars.discordLink.get()))
                                            .hoverEvent(Component.text("Click to join :D"))
                            )
                            .build()
            );
            return 0;
        })
                .then(literal("unlink")
                        .executes(c -> {
                            CommandUnlink.execute(c.getSource().asPlayer(), new String[0]);
                            return 0;
                        })
                )

                .then(literal("link")
                        .executes(c -> {
                            CommandLink.execute(c.getSource().asPlayer(), new String[0]);
                            return 0;
                        })
                );
    }
}
