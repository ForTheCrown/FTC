package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.*;
import net.forthecrown.user.CrownUser;

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
                    CrownUser user = getUserSender(c);

                    RegionUtil.validateWorld(user.getWorld());

                    RegionManager manager = Crown.getRegionManager();
                    PopulationRegion region = manager.get(user.getRegionPos());
                    RegionResidency residency = region.getResidency();

                    if(region.hasProperty(RegionProperty.HIDE_RESIDENTS)
                            && !user.hasPermission(Permissions.REGIONS_ADMIN)
                    ) {
                        throw FtcExceptionProvider.translatable("regions.reside.hidden");
                    }

                    if(residency.isEmpty()) {
                        throw FtcExceptionProvider.translatable("regions.reside.none");
                    }

                    ComponentWriter writer = ComponentWriter.normal();
                    residency.write(writer);

                    user.sendMessage(writer.get());
                    return 0;
                });
    }
}