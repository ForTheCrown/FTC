package net.forthecrown.dungeons.level;

import net.forthecrown.registry.Registries;
import org.bukkit.Location;
import org.bukkit.World;

public class DungeonLevels {
    public static DungeonLevel find(Location l) {
        return find(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public static DungeonLevel find(World w, int x, int y, int z) {
        for (DungeonLevel l: Registries.DUNGEON_LEVELS) {
            if(!l.getWorld().equals(w) || !l.getBounds().contains(x, y, z)) continue;
            return l;
        }

        return null;
    }
}
