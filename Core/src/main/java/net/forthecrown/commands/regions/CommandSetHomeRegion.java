package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandSetHomeRegion extends FtcCommand {

    public CommandSetHomeRegion() {
        super("sethomeregion");

        setAliases("sethomepole", "sethomepost");
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
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    RegionUtil.validateWorld(user.getWorld());

                    RegionPos cords = user.getRegionCords();

                    user.getHomes().setHomeRegion(cords);

                    user.sendMessage(Component.translatable("regions.setHome", NamedTextColor.YELLOW));
                    return 0;
                });
    }
}