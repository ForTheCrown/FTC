package net.forthecrown.commands.punishments;

import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public class CommandSmite extends FtcCommand {

    public CommandSmite() {
        super("smite");

        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Smites a player with lightning
     *
     * Valid usages of command:
     * /smite <player>
     *
     * Permissions used:
     * ftc.admin
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.onlineUser())
                        .executes(c -> {
                            CrownUser user = UserType.getUser(c, "user");

                            user.getWorld().strikeLightning(user.getLocation());

                            c.getSource().sendMessage(Component.text("Smiting ").append(user.displayName()));
                            return 0;
                        })
                );
    }
}