package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;

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
                    CrownUser user = getUserSender(c);
                    RegionUtil.validateWorld(user.getWorld());

                    RegionManager manager = Crown.getRegionManager();
                    PopulationRegion region = manager.get(user.getRegionPos());
                    manager.getGenerator().generate(region);

                    c.getSource().sendAdmin("Placed region pole at " + region.nameOrPos());
                    return 0;
                });
    }
}