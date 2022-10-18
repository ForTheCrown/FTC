package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;

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
            User user = getUserSender(c);
            RegionPos cords = user.getRegionPos();
            PopulationRegion region = RegionManager.get().get(cords);

            if(region.hasName()) {
                user.sendMessage(Messages.whichRegionNamed(region));
                return 0;
            }

            throw Exceptions.IN_UNNAMED_REGION;
        });
    }
}