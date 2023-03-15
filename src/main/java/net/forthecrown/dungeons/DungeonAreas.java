package net.forthecrown.dungeons;

import net.forthecrown.core.Worlds;
import net.forthecrown.utils.math.WorldBounds3i;

public interface DungeonAreas {

  WorldBounds3i DUNGEON_AREA  = create(  -15 + 202,  16 - 48,  295 - 49,   -332 + 202,  92 - 48,    5 - 49);
  WorldBounds3i ZHAMBIE_ROOM  = create( -174 + 202,  92 - 48,  171 - 49,   -207 + 202,  76 - 48,  142 - 49);
  WorldBounds3i SKALATAN_ROOM = create( -114 + 202,  80 - 48,  173 - 49,    -91 + 202,  62 - 48,  195 - 49);
  WorldBounds3i SPIDEY_ROOM   = create(  -64 + 202,  65 - 48,  296 - 49,    -95 + 202,  52 - 48,  273 - 49);
  WorldBounds3i DRAWNED_ROOM  = create( -139 + 202,  18 - 48,   23 - 49,   -109 + 202,  46 - 48,   52 - 49);
  WorldBounds3i EVOKER_ROOM   = create( -296 + 202,  32 - 48,   26 - 49,   -260 + 202,  48 - 48,   62 - 49);

  private static WorldBounds3i create(int x, int y, int z, int x1, int y1, int z1) {
    return new WorldBounds3i(Worlds.voidWorld(), x, y, z, x1, y1, z1);
  }
}