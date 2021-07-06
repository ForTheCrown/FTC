package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandIgnoreAC extends FtcCommand {

    public CommandIgnoreAC() {
        super("ignorebroadcasts");

        setPermission(Permissions.DEFAULT);
        setDescription("Allows you to toggle ignoring announcements");
        setAliases("ignoreac", "ignorebc", "ignoreannouncements");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows you to ignore broadcasts
     *
     * Valid usages of command:
     * /ignorebc
     * /ignorebroadcasts
     * /ignoreannouncements
     * /ignoreac
     *
     * Permissions used:
     * ftc.default
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    boolean ignoring = !user.ignoringBroadcasts();

                    Component message = Component.translatable("user.acIgnore." + (ignoring ? "on" : "off"), NamedTextColor.GRAY);

                    user.setIgnoringBroadcasts(ignoring);
                    user.sendMessage(message);

                    return 0;
                });
    }
}