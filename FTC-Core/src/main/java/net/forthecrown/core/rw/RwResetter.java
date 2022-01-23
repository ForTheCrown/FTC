package net.forthecrown.core.rw;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Worlds;
import net.forthecrown.regions.RegionData;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.utils.world.WorldReCreator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RwResetter {
    public static boolean shouldReset() {
        if(ComVars.nextResourceWorldReset() == -1) return false;
        return TimeUtil.isPast(ComVars.nextResourceWorldReset());
    }

    public static void reset() {
        kickPlayers();
        WorldReCreator creator = WorldReCreator.of(Worlds.RESOURCE)
                .preserveWorldBorder(true);
                //.generator(new RwGenerator());

        World world = creator.run();
        ComVars.nextResourceWorldReset(System.currentTimeMillis() + ComVars.resourceWorldResetInterval());

        WorldLoader.loadAsync(world).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                Crown.logger().error("Could not regen Resource World", throwable);
            }
        });
    }

    static void kickPlayers() {
        RegionData spawnData = Crown.getRegionManager().get(ComVars.getSpawnRegion());

        BlockVector2 pos = spawnData.getPolePosition();
        int y = Worlds.OVERWORLD.getHighestBlockYAt(pos.getX(), pos.getZ());

        Location exitLocation = new Location(Worlds.OVERWORLD, pos.getX(), y, pos.getZ());

        for (Player p: Worlds.RESOURCE.getPlayers()) {
            p.teleport(exitLocation);
        }
    }
}
