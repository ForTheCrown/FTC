package net.forthecrown.dungeons;

import net.forthecrown.core.Worlds;
import net.forthecrown.utils.transformation.FtcBoundingBox;

public interface DungeonAreas {
    FtcBoundingBox
            DUNGEON_AREA       = create(-15, 16, 295, -255, 92, 26),
            ZHAMBIE_ROOM       = create(-174, 92, 171, -207, 76, 142),
            SKALATAN_ROOM      = create(-114, 80, 173, -91, 62, 195),
            SPIDEY_ROOM        = create(-64, 65, 296, -95, 52, 273),
            DRAWNED_ROOM       = create(-139, 18, 23, -109, 46, 52);

    private static FtcBoundingBox create(int x, int y, int z, int x1, int y1, int z1) {
        return new FtcBoundingBox(Worlds.voidWorld(), x, y, z, x1, y1, z1);
    }
}