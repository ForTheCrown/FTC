package net.forthecrown.core.rw;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Worlds;
import net.forthecrown.regions.RegionData;
import net.forthecrown.utils.TimeUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;

public class RwResetter {
    public static boolean shouldReset() {
        if(ComVars.nextResourceWorldReset() == -1) return false;
        return TimeUtil.isPast(ComVars.nextResourceWorldReset());
    }

    public static void reset() {
        kickPlayers();
        File dir = Worlds.RESOURCE.getWorldFolder();
        Bukkit.unloadWorld(Worlds.RESOURCE, true);

        dir.delete();

        WorldCreator creator = new WorldCreator("world_resource")
                .generateStructures(true)
                .environment(World.Environment.NORMAL)
                .type(WorldType.NORMAL)
                .generator(new RwGenerator());

        World world = creator.createWorld();

        RwLoader.load(world);
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
