package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.*;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.user.User;

public class CommandRegionResidents extends FtcCommand {

    public CommandRegionResidents() {
        super("RegionResidents");

        setAliases("residents");
        setPermission(Permissions.REGIONS);
        setDescription("Shows you who lives in the current region you're in");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /RegionResidents
     *
     * Permissions used:
     * ftc.regions
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
                    RegionResidency residency = region.getResidency();

                    if(region.hasProperty(RegionProperty.HIDE_RESIDENTS)
                            && !user.hasPermission(Permissions.REGIONS_ADMIN)
                    ) {
                        throw Exceptions.RESIDENTS_HIDDEN;
                    }

                    if (residency.isEmpty()) {
                        throw Exceptions.NOTHING_TO_LIST;
                    }

                    TextWriter writer = TextWriters.newWriter();
                    residency.write(writer);

                    user.sendMessage(writer.asComponent());
                    return 0;
                });
    }
}