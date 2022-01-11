package net.forthecrown.commands.help;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class HelpPost extends FtcCommand {

    public HelpPost(){
        super("posthelp", Crown.inst());

        setAliases("polehelp");
        setPermission(Permissions.HELP);
        setDescription("Displays info for region poles.");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Displays information about region poles.
     *
     *
     * Valid usages of command:
     * - /posthelp
     * - /polehelp
     *
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix
     * - Findpole
     *
     * Author: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            CommandSender sender = c.getSource().asBukkit();

            sender.sendMessage(
                    Component.text()
                            .append(Crown.prefix())
                            .append(Component.text("Info about poles: ").color(NamedTextColor.YELLOW))

                            .append(line("Use", "findpole", "to find the closest pole"))
                            .append(line("Use", "visit", "to travel between them"))
                            .append(line("Use", "movein", "to make a pole your home"))
                            .append(line("Then use", "home", "to go there"))
            );

            return 0;
        });
    }

    private Component line(String pre, String cmd, String post) {
        return Component.text()
                .append(Component.newline())
                .append(Component.text(pre))
                .append(Component.text(" [" + cmd + "] ")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(Component.text("Click me :D"))
                        .clickEvent(ClickEvent.suggestCommand("/" + cmd))
                )
                .append(Component.text(post + "."))
                .build();
    }
}
