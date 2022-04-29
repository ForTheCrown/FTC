package net.forthecrown.dungeons;

import com.sk89q.worldedit.math.Vector3;
import net.forthecrown.core.Worlds;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.Location;

public interface DungeonConstants {
    Vector3 SPIDEY_SPAWN_VECTOR = Vector3.at(-78.5, 55, 284.5);

    Location
            ZHAMBIE_SPAWN      = createSpawn(-191.5,   80, 157.5),
            SKALATAN_SPAWN     = createSpawn(-103.5,   67, 184.5),
            SPIDEY_SPAWN       = createSpawn( -78.5,   55, 284.5),
            DRAWNED_SPAWN      = createSpawn(-123.5, 25.5,  38.5),
            EVOKER_SPAWN       = createSpawn(-277.5,   37,  44.5);

    WorldBounds3i
            DUNGEON_AREA       = create( -15, 16, 295, -255, 92,  26),
            ZHAMBIE_ROOM       = create(-174, 92, 171, -207, 76, 142),
            SKALATAN_ROOM      = create(-114, 80, 173,  -91, 62, 195),
            SPIDEY_ROOM        = create( -64, 65, 296,  -95, 52, 273),
            DRAWNED_ROOM       = create(-139, 18,  23, -109, 46,  52),
            EVOKER_ROOM        = create(-296, 32,  26, -260, 48,  62);

    long
            ID_ZHAMBIE         = 1L,
            ID_SKALATAN        = 2L,
            ID_SPIDEY          = 3L,
            ID_DRAWNED         = 4L,
            ID_EMO             = 5L;

    private static WorldBounds3i create(int x, int y, int z, int x1, int y1, int z1) {
        return new WorldBounds3i(Worlds.voidWorld(), x, y, z, x1, y1, z1);
    }

    private static Location createSpawn(double x, double y, double z) {
        return new Location(Worlds.voidWorld(), x, y, z);
    }
}