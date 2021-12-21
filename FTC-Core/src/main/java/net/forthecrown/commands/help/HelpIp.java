package net.forthecrown.commands.help;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class HelpIp extends FtcCommand {

    public HelpIp() {
        super("Ip");

        setPermission(Permissions.DEFAULT);
        setDescription("Shows the server's IP");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Why
     *
     * Valid usages of command:
     * /Ip
     *
     * Permissions used:
     *
     * Main Author: Jules
     */

    private static final Component MESSAGE = Component.text("Server's IP:")
            .color(NamedTextColor.YELLOW)
            .append(Component.newline())
            .append(
                    Component.text("mc.forthecrown.net (Click to copy)")
                            .color(NamedTextColor.AQUA)
                            .hoverEvent(Component.text("Click to copy"))
                            .clickEvent(ClickEvent.copyToClipboard("mc.forthecrown.net"))
            );

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            c.getSource().sendMessage(MESSAGE);
            return 0;
        });
    }
}