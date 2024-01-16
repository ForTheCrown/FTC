package net.forthecrown.economy.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Getter;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.AreaSelection;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.block.Block;
import org.spongepowered.math.vector.Vector3i;

/**
 * A copy and paste of a region from one place to another
 */
public class RegionCopyPaste implements Runnable {

  @Getter
  private final WorldVec3i destination;

  @Getter
  private final AreaSelection origin;

  private List<CopyPreProcessor> preProcessors = null;
  private List<BlockFilter> blockFilters = null;

  public RegionCopyPaste(WorldVec3i destination, AreaSelection origin) {
    this.destination = destination;
    this.origin = origin;
  }

  public static RegionCopyPaste create(AreaSelection area, WorldVec3i destination) {
    return new RegionCopyPaste(destination, area);
  }

  public RegionCopyPaste addPreProcessor(CopyPreProcessor processor) {
    if (preProcessors == null) {
      preProcessors = new ObjectArrayList<>();
    }

    preProcessors.add(processor);
    return this;
  }

  public RegionCopyPaste addFilter(BlockFilter filter) {
    if (blockFilters == null) {
      blockFilters = new ObjectArrayList<>();
    }

    blockFilters.add(filter);
    return this;
  }

  @Override
  public void run() {
    Tasks.runSync(runnable());
  }

  private Runnable runnable() {
    return () -> {
      Vector3i minPos = origin.min();

      for (Block b : origin) {
        // Get the offset of the block in relation
        // to the minimum point of the origin
        var offset = Vectors.from(b).sub(minPos);

        //The destination of the block in pasting
        var destPos = destination.add(offset);
        var destBlock = Vectors.getBlock(destPos, destination.getWorld());

        //If filters don't like this block lol
        if (!testBlock(b, destBlock)) {
          continue;
        }

        runPreprocessors(b, destBlock);
        destBlock.setBlockData(b.getBlockData().clone(), true);
      }
    };
  }

  private boolean testBlock(Block copy, Block paste) {
    if (blockFilters == null || blockFilters.isEmpty()) {
      return true;
    }

    for (BlockFilter f : blockFilters) {
      if (!f.test(copy, paste, this)) {
        return false;
      }
    }

    return true;
  }

  private void runPreprocessors(Block copy, Block paste) {
    if (preProcessors == null || preProcessors.isEmpty()) {
      return;
    }

    preProcessors.forEach(processor -> processor.process(copy, paste, this));
  }
}