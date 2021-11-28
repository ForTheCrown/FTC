package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collection;
import java.util.Iterator;

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
                    TextComponent.Builder builder = Component.text()
                            .color(NamedTextColor.YELLOW)
                            .append(Component.translatable("regions.list"))
                            .append(Component.text(": "));

                    RegionManager manager = Crown.getRegionManager();
                    Collection<PopulationRegion> regions = manager.getNamedRegions();
                    Iterator<PopulationRegion> iterator = regions.iterator();

                    while (iterator.hasNext()) {
                        PopulationRegion r = iterator.next();

                        builder.append(r.displayName().colorIfAbsent(NamedTextColor.AQUA));

                        if(iterator.hasNext()) builder.append(Component.text(", "));
                    }


                    c.getSource().sendMessage(builder.build());
                    return 0;
                });
    }
}