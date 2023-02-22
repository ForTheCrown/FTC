package net.forthecrown.dungeons.level.placement;

import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.structure.FunctionInfo;

@Getter
@RequiredArgsConstructor
public class PoolProcessor implements PostPlacementProcessor {

  @Override
  public void processAll(
      LevelPlacement placement,
      List<FunctionInfo> markerList,
      Random random
  ) {

  }
}