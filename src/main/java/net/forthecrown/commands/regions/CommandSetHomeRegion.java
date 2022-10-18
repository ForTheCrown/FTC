package net.forthecrown.commands.regions;

import net.forthecrown.text.Messages;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.Regions;
import net.forthecrown.user.User;

public class CommandSetHomeRegion extends FtcCommand {

    public CommandSetHomeRegion() {
        super("sethomeregion");

        setAliases("sethomepole", "sethomepost", "movein");
        setPermission(Permissions.REGIONS);
        setDescription("Sets your home region");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /sethomeregion
     * /sethomepole
     * /sethomepost
     *
     * Permissions used: ftc.regions
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    Regions.validateWorld(user.getWorld());

                    RegionPos cords = user.getRegionPos();

                    user.getHomes().setHomeRegion(cords);

                    user.sendMessage(Messages.HOME_REGION_SET);
                    return 0;
                });
    }
}