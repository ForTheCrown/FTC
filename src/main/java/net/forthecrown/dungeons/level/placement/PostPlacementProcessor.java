package net.forthecrown.dungeons.level.placement;

import java.util.List;
import java.util.Random;
import net.forthecrown.structure.FunctionInfo;
import org.bukkit.World;

public interface PostPlacementProcessor {
  void processAll(World world, List<FunctionInfo> markerList, Random random);
}