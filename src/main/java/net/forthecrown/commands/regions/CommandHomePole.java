package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.*;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.user.User;

public class CommandHomePole extends FtcCommand {

    public CommandHomePole() {
        super("homepole");

        setAliases("homepost", "homeregion");
        setPermission(Permissions.REGIONS);
        setDescription("Takes you to your home region");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Takes you to your home pole, if you have one
     *
     * Valid usages of command:
     * /HomePole
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    Regions.validateWorld(user.getWorld());

                    RegionPos cords = user.getHomes().getHomeRegion();

                    if (cords == null) {
                        throw Exceptions.NO_HOME_REGION;
                    }

                    RegionPos local = user.getRegionPos();
                    RegionAccess localRegion = RegionManager.get().get(local);
                    Regions.validateDistance(localRegion.getPolePosition(), user);

                    PopulationRegion region = RegionManager.get().get(cords);

                    RegionVisit.visitRegion(user, region);
                    return 0;
                });
    }
}