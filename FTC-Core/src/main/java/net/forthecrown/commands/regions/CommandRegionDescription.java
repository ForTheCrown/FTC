package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.arguments.RegionArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.kyori.adventure.text.Component;

public class CommandRegionDescription extends FtcCommand {

    public CommandRegionDescription() {
        super("regiondescription");

        setAliases("regioninfo");
        setPermission(Permissions.REGIONS_ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /RegionDescription
     *
     * Permissions used: ftc.regions.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
         command
                 .then(argument("region", RegionArgument.region())
                         .then(argument("desc", ChatArgument.chat())
                                 .executes(c -> {
                                     Component desc = c.getArgument("desc", Component.class);
                                     PopulationRegion region = RegionArgument.regionInviteIgnore(c, "region");
                                     if(!region.hasName()) throw FtcExceptionProvider.create("Only named regions may have descriptions");

                                     region.setDescription(desc);

                                     c.getSource().sendAdmin(
                                             Component.text("Set description of ")
                                                     .append(region.displayName())
                                                     .append(Component.text(" to "))
                                                     .append(desc)
                                     );
                                     return 0;
                                 })
                         )
                 );
    }
}