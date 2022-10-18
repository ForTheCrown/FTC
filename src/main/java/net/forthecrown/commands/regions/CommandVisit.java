package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.Regions;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;

public class CommandVisit extends FtcCommand {
    private static final String COOLDOWN_CATEGORY = "Commands_Visit";

    public CommandVisit() {
        super("visit");

        setPermission(Permissions.REGIONS);
        setAliases("vr", "visitpost", "visitregion", "visitpole");
        setDescription("Visits a player's or a specific region");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /visit <region>
     * /visit <player>
     *
     * Permissions used: ftc.regions
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("region", Arguments.REGION)
                        .executes(c -> {
                            User user = getUserSender(c);

                            Cooldown.testAndThrow(user, COOLDOWN_CATEGORY, 5 * 20);
                            Regions.validateWorld(user.getWorld());

                            PopulationRegion region = Arguments.getRegion(c, "region", true);
                            var closest = RegionManager.get().getAccess(user.getRegionPos());
                            Regions.validateDistance(closest.getPolePosition(), user);

                            RegionVisit.visitRegion(user, region);
                            return 0;
                        })
                );
    }
}