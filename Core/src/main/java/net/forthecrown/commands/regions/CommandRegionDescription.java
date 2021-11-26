package net.forthecrown.commands.regions;

import net.forthecrown.commands.admin.CommandLore;
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
                 .then(CommandLore.compOrStringArg(
                         argument("region", RegionArgument.region()),
                         (context, builder) -> RegionArgument.region().listSuggestions(context, builder),

                         (c, lore) -> {
                             PopulationRegion region = RegionArgument.regionInviteIgnore(c, "region");
                             if(!region.hasName()) throw FtcExceptionProvider.create("Only named regions may have descriptions");

                             region.setDescription(lore);

                             c.getSource().sendAdmin(
                                     Component.text("Set description of ")
                                            .append(region.displayName())
                                            .append(Component.text(" to "))
                                            .append(lore)
                             );
                             return 0;
                         }
                 ));
    }
}