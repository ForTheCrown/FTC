package net.forthecrown.dungeons;

import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.FtcBoundingBox;

public interface DungeonAreas {
    FtcBoundingBox
            DUNGEON_AREA       = new FtcBoundingBox(Worlds.VOID, -15, 16, 295, -255, 92, 26),
            ZHAMBIE_ROOM       = new FtcBoundingBox(Worlds.VOID, -174, 92, 171, -207, 76, 142),
            SKALATAN_ROOM      = new FtcBoundingBox(Worlds.VOID, -114, 80, 173, -91, 62, 195),
            SPIDEY_ROOM        = new FtcBoundingBox(Worlds.VOID, -64, 65, 296, -95, 52, 273),
            DRAWNED_ROOM       = new FtcBoundingBox(Worlds.VOID, -139, 18, 23, -109, 46, 52);
}