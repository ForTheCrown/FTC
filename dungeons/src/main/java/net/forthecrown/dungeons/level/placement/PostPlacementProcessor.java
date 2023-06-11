package net.forthecrown.dungeons.level.placement;

import java.util.List;
import java.util.Random;
import net.forthecrown.structure.FunctionInfo;

public interface PostPlacementProcessor {
  void processAll(
      LevelPlacement placement,
      List<FunctionInfo> markerList,
      Random random
  );
}