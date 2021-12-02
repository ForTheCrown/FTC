package net.forthecrown.commands.regions;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;

public class CommandRenameRegion extends FtcCommand {

    public CommandRenameRegion() {
        super("renameregion");

        setAliases("nameregion");
        setPermission(Permissions.REGIONS_ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /renameregion <name>
     *
     * Permissions used: ftc.regions.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("name", StringArgumentType.word())
                        .suggests(suggestMatching("-clear"))

                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            RegionUtil.validateWorld(user.getWorld());

                            String name = c.getArgument("name", String.class);

                            RegionManager manager = Crown.getRegionManager();
                            PopulationRegion region = manager.get(user.getRegionCords());

                            if(name.equals("-clear")) {
                                manager.rename(region, null);
                                c.getSource().sendAdmin("Removed name of region " + name);
                            } else {
                                manager.rename(region, name);
                                c.getSource().sendAdmin("Set name of region to " + name);
                            }

                            return 0;
                        })
                );
    }
}