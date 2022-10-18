package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

public class CommandMoveToRegion extends FtcCommand {

    public CommandMoveToRegion() {
        super("movetoregion");

        setAliases("sendtoregion", "sendregion");
        setPermission(Permissions.REGIONS_ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /MoveToRegion
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.ONLINE_USER)
                        .then(argument("region", Arguments.REGION)
                                .executes(c -> {
                                    User user = Arguments.getUser(c, "user");
                                    PopulationRegion region = Arguments.regionInviteIgnore(c, "region");

                                    RegionVisit.visitRegion(user, region);

                                    c.getSource().sendAdmin(
                                            Component.text("Sent ")
                                                    .append(user.displayName())
                                                    .append(Component.text(" to "))
                                                    .append(region.displayName())
                                    );
                                    return 0;
                                })
                        )
                );
    }
}