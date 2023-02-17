package net.forthecrown.dungeons.level.placement;

import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.structure.FunctionInfo;
import net.forthecrown.structure.StructurePool;
import org.bukkit.World;

@Getter
@RequiredArgsConstructor
public class PoolProcessor implements PostPlacementProcessor {
  private final StructurePool pool;

  @Override
  public void processAll(World world,
                         List<FunctionInfo> markerList,
                         Random random
  ) {

  }
}