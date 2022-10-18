package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.Regions;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.math.WorldBounds3i;

import java.util.Random;

public class CommandRandomRegion extends FtcCommand {
    private static final int COOLDOWN_IN_SECONDS = 10;

    public CommandRandomRegion() {
        super("randomregion");

        setPermission(Permissions.REGIONS_ADMIN);
        setDescription("Takes you to a random region");

        register();
    }

    private final Random random = new Random();

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /RandomRegion
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

                    Cooldown.testAndThrow(user, getName(), COOLDOWN_IN_SECONDS * 20);

                    WorldBounds3i box = WorldBounds3i.of(user.getWorld());
                    RegionPos max = RegionPos.of(box.max());
                    RegionPos min = RegionPos.of(box.min());

                    RegionPos cords = new RegionPos(
                            random.nextInt(min.getX(), max.getX() - 1),
                            random.nextInt(min.getZ(), max.getZ() - 1)
                    );

                    PopulationRegion region = RegionManager.get().get(cords);

                    RegionVisit.visitRegion(user, region);
                    return 0;
                });
    }
}