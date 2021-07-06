package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandToggleRiding extends FtcCommand {

    public CommandToggleRiding() {
        super("toggleriding");

        setAliases("riding", "ridingtoggle");
        setPermission(Permissions.DEFAULT);
        setDescription("Toggle whether people can ride you");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows someone to toggle whether to allow player riding
     *
     * Valid usages of command:
     * /player riding
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
                    boolean allows = !user.allowsRiding();

                    user.sendMessage(Component.translatable("user.riding." + (allows ? "allow" : "deny"), allows ? NamedTextColor.YELLOW : NamedTextColor.GRAY));
                    user.setAllowsRiding(allows);

                    return 0;
                });
    }
}