package net.forthecrown.user;

import net.forthecrown.core.Crown;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPoleGenerator;
import net.forthecrown.regions.RegionPos;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.UUID;

final class UserUtil {
    private UserUtil() {}

    private static final Logger LOGGER = Crown.logger();

    static void reassignHome(UUID uuid, String name, @Nullable Location old, @Nullable Location newHome) {
        if (old == null && newHome == null) {
            return;
        }

        RegionManager manager = Crown.getRegionManager();
        RegionPoleGenerator generator = manager.getGenerator();

        /*if (inSameRegion(old, newHome, manager)) {
            return;
        }*/

        if (old != null && old.getWorld().equals(manager.getWorld())) {
            RegionPos pos = RegionPos.of(old);
            PopulationRegion region = manager.get(pos);

            region.getResidency()
                    .getEntry(uuid)
                    .removeHome(name);

            generator.generate(region);
        }

        if (newHome != null && newHome.getWorld().equals(manager.getWorld())) {
            RegionPos pos = RegionPos.of(newHome);
            PopulationRegion region = manager.get(pos);

            region.getResidency()
                    .getEntry(uuid)
                    .addHome(name, System.currentTimeMillis());

            generator.generate(region);
        }
    }

    static boolean inSameRegion(Location l1, Location l2, RegionManager manager) {
        // If either is null
        if (l1 == null || l2 == null) return false;

        World mWorld = manager.getWorld();

        // If either isn't in the managed world
        if (!l1.getWorld().equals(mWorld) || !l2.getWorld().equals(mWorld)) {
            return false;
        }

        RegionPos p1 = RegionPos.of(l1);
        RegionPos p2 = RegionPos.of(l2);

        return p1.equals(p2);
    }

    static void reassignRegionHome(UUID uuid, @Nullable RegionPos old, @Nullable RegionPos newPos) {
        RegionManager manager = Crown.getRegionManager();

        if (old != null) {
            PopulationRegion region = manager.get(old);

            region.getResidency()
                    .getEntry(uuid)
                    .setDirectMoveIn(0L);

            manager.getGenerator().generate(region);
        }

        if (newPos != null) {
            PopulationRegion region = manager.get(newPos);

            region.getResidency()
                    .getEntry(uuid)
                    .setDirectMoveIn(System.currentTimeMillis());

            LOGGER.info("Set new region home: {} for user '{}'", region.nameOrPos(), uuid);
            manager.getGenerator().generate(region);
        }
    }
}