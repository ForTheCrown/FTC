package net.forthecrown.core.transformers;

import net.forthecrown.core.Crown;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.RegionResidency;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUserHomes;
import net.forthecrown.user.UserHomes;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;

public class RegionResidencyTransformer {
    private static final Logger LOGGER = Crown.logger();

    public static boolean shouldRun() {
        return Transformers.shouldRun(RegionResidencyTransformer.class);
    }

    public static void run() {
        LOGGER.info("Running region residency transformer");

        FtcUserHomes.DISABLE_LIMIT_CHECK = true;

        Crown.getUserManager().getAllUsers().whenComplete(
                (users, throwable) -> {
                    if(throwable != null) {
                        LOGGER.error("Error when loading users", throwable);
                        return;
                    }

                    Bukkit.getScheduler().runTask(Crown.inst(), () -> withUsers(users));
                }
        );
    }

    private static void withUsers(Collection<CrownUser> users) {
        LOGGER.info("Got all users, starting transformer");

        RegionManager manager = Crown.getRegionManager();

        for (CrownUser user: users) {
            LOGGER.info("Starting transformer on {}", user.getName());

            try {
                UserHomes homes = user.getHomes();

                for (var v: homes.getHomes().entrySet()) {
                    String name = v.getKey();
                    Location l = v.getValue();

                    if (!l.getWorld().equals(manager.getWorld())) {
                        continue;
                    }

                    manager.get(RegionPos.of(l))
                            .getResidency()
                            .getEntry(user.getUniqueId())
                            .addHome(name, -1);
                }

                if (homes.hasHomeRegion()) {
                    PopulationRegion region = manager.get(homes.getHomeRegion());
                    RegionResidency.ResEntry entry = region.getResidency().getEntry(user.getUniqueId());

                    entry.setDirectMoveIn(-1);
                    manager.getGenerator().generate(region);
                }
            } catch (Throwable e) {
                LOGGER.error("Error converting data", e);
                continue;
            }

            LOGGER.info("Completed transformer on {}", user.getName());
        }

        Crown.getUserManager().unloadOffline();
        FtcUserHomes.DISABLE_LIMIT_CHECK = false;

        Transformers.complete(RegionResidencyTransformer.class);
    }
}