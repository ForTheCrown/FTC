package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.text.Messages;

import java.util.Collection;

public class CommandListRegions extends FtcCommand {

    public CommandListRegions() {
        super("listregions");

        setPermission(Permissions.REGIONS);
        setAliases("regionlist", "regionslist", "allregions");
        setDescription("Lists all named regions");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /ListRegions
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    RegionManager manager = RegionManager.get();
                    Collection<PopulationRegion> regions = manager.getNamedRegions();

                    c.getSource().sendMessage(Messages.listRegions(regions));
                    return 0;
                });
    }
}