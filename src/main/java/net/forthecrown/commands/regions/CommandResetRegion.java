package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.Regions;
import net.forthecrown.user.User;

public class CommandResetRegion extends FtcCommand {

    public CommandResetRegion() {
        super("resetregion");

        setPermission(Permissions.REGIONS_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /resetregion
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

                    RegionManager manager = RegionManager.get();

                    PopulationRegion region = manager.get(user.getRegionPos());
                    String name = region.nameOrPos();

                    manager.reset(region);

                    c.getSource().sendAdmin("Reset region " + name);
                    return 0;
                });
    }
}