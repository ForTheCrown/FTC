package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.Regions;
import net.forthecrown.user.User;

public class CommandAddPole extends FtcCommand {

    public CommandAddPole() {
        super("addpole");

        setAliases("addpost", "addregionpole", "addregionpost");
        setPermission(Permissions.REGIONS_ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds a pole the region the executor is
     * standing in
     *
     * Valid usages of command:
     * /AddPole
     *
     * Permissions used: ftc.regions.admin
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
                    Regions.placePole(region);

                    c.getSource().sendAdmin("Placed region pole at " + region.nameOrPos());
                    return 0;
                });
    }
}