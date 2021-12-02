package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.RegionArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.ActionFactory;
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
                .then(argument("region", RegionArgument.region())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);

                            if(Cooldown.containsOrAdd(user, COOLDOWN_CATEGORY, 5 * 20)) {
                                throw FtcExceptionProvider.translatable("commands.visitTooSoon");
                            }

                            PopulationRegion region = RegionArgument.getRegion(c, "region", true);
                            PopulationRegion closest = Crown.getRegionManager().get(user.getRegionCords());

                            RegionUtil.validateWorld(user.getWorld());
                            RegionUtil.validateDistance(closest.getPolePosition(), user);

                            ActionFactory.visitRegion(user, region);
                            return 0;
                        })
                );
    }
}