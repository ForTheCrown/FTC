package net.forthecrown.commands.regions;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandWhichRegion extends FtcCommand {

    public CommandWhichRegion() {
        super("whichregion");

        setPermission(Permissions.REGIONS);
        setAliases("getregion");
        setDescription("Tells you which region you're currently in");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /WhichRegion
     *
     * Permissions used:
     * ftc.regions
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            RegionPos cords = user.getRegionCords();
            PopulationRegion region = Crown.getRegionManager().get(cords);

            if(region.hasName()) {
                user.sendMessage(
                        Component.translatable("regions.which.name",
                                NamedTextColor.GOLD,
                                region.displayName()
                                        .colorIfAbsent(NamedTextColor.YELLOW)
                        )
                );
            } else {
                if(!user.hasPermission(Permissions.REGIONS_ADMIN)) throw FtcExceptionProvider.translatable("regions.which.none");

                BlockVector2 pos = cords.toCenter();
                user.sendMessage(
                        Component.translatable("regions.which.cords",
                                NamedTextColor.GOLD,
                                Component.text(pos.getX() + " " + pos.getZ())
                        )
                );
            }

            return 0;
        });
    }
}