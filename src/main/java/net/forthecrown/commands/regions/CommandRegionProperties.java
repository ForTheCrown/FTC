package net.forthecrown.commands.regions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionProperty;
import net.kyori.adventure.text.Component;

public class CommandRegionProperties extends FtcCommand {

    public CommandRegionProperties() {
        super("RegionProperties");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /RegionProperties
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("region", Arguments.REGION)
                        .then(argument("property", EnumArgument.of(RegionProperty.class))
                                .executes(c -> {
                                    PopulationRegion region = Arguments.regionInviteIgnore(c, "region");
                                    RegionProperty property = c.getArgument("property", RegionProperty.class);

                                    c.getSource().sendMessage(
                                            region.displayName()
                                                    .append(Component.text(" has property " + property.name().toLowerCase() + ": "))
                                                    .append(Component.text(region.hasProperty(property) + ""))
                                    );
                                    return 0;
                                })

                                .then(literal("add")
                                        .executes(c -> property(c, true))
                                )

                                .then(literal("remove")
                                        .executes(c -> property(c, false))
                                )
                        )
                );
    }

    private int property(CommandContext<CommandSource> c, boolean state) throws CommandSyntaxException {
        PopulationRegion region = Arguments.regionInviteIgnore(c, "region");
        RegionProperty property = c.getArgument("property", RegionProperty.class);

        region.setProperty(property, state);

        c.getSource().sendAdmin(
                Component.text()
                        .append(region.displayName())
                        .append(Component.text("'s property " + property.name().toLowerCase() + " is now " + state))
                        .build()
        );
        return 0;
    }
}