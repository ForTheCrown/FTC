package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.RegionArgument;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.RegionVisit;
import net.forthecrown.user.actions.UserActionHandler;
import net.kyori.adventure.text.Component;

public class CommandMoveToRegion extends FtcCommand {

    public CommandMoveToRegion() {
        super("movetoregion");

        setAliases("sendtoregion");
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
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.onlineUser())
                        .then(argument("region", RegionArgument.region())
                                .executes(c -> {
                                    CrownUser user = UserArgument.getUser(c, "user");
                                    PopulationRegion region = RegionArgument.regionInviteIgnore(c, "region");

                                    RegionVisit action = new RegionVisit(user, region);
                                    UserActionHandler.handleAction(action);

                                    c.getSource().sendAdmin(
                                            Component.text("Sent ")
                                                    .append(user.nickDisplayName())
                                                    .append(Component.text(" to "))
                                                    .append(region.displayName())
                                    );
                                    return 0;
                                })
                        )
                );
    }
}