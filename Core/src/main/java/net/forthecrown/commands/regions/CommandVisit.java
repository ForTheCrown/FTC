package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.RegionArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.RegionVisit;
import net.forthecrown.user.actions.UserActionHandler;

public class CommandVisit extends FtcCommand {

    public CommandVisit() {
        super("visit");

        setPermission(Permissions.REGIONS);
        setAliases("vr", "visitpost", "visitregion", "visitpole");
        setDescription("Visits a player's or a specific region");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /visit <region>
     * /visit <player>
     *
     * Permissions used: ftc.regions
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("region", RegionArgument.region())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            PopulationRegion region = RegionArgument.getRegion(c, "region", true);

                            RegionUtil.validateWorld(user.getWorld());
                            RegionUtil.validateDistance(region.getPolePosition(), user);

                            RegionVisit action = new RegionVisit(user, region);
                            UserActionHandler.handleAction(action);

                            return 0;
                        })
                );
    }
}