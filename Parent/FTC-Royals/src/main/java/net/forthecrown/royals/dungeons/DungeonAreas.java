package net.forthecrown.royals.dungeons;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.utils.CrownUtils;
import org.bukkit.World;

public class DungeonAreas {
    public static final World WORLD = CrownUtils.WORLD_VOID;
    public static final CrownBoundingBox DUNGEON_AREA = new CrownBoundingBox(WORLD, -15, 16, 295, -255, 92, 26);

    public static final CrownBoundingBox ZHAMBIE_ROOM = new CrownBoundingBox(WORLD, -174, 92, 171, -207, 76, 142);
    public static final CrownBoundingBox SKALATAN_ROOM = new CrownBoundingBox(WORLD, -114, 80, 173, -91, 62, 195);
    public static final CrownBoundingBox SPIDEY_ROOM = new CrownBoundingBox(WORLD, -64, 65, 296, -95, 52, 273);
    public static final CrownBoundingBox DRAWNED_ROOM = new CrownBoundingBox(WORLD, -139, 18, 23, -109, 46, 52);
}