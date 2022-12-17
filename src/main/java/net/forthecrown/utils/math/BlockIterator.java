package net.forthecrown.utils.math;

import java.util.NoSuchElementException;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.spongepowered.math.vector.Vector3i;

public class BlockIterator extends AbstractPosIterator<Block> {

  private final World world;

  public BlockIterator(World world, Vector3i min, Vector3i max, long maxIteration) {
    super(min, max, maxIteration);
    this.world = world;
  }

  @Override
  public Block next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    advance();
    return world.getBlockAt(x, y, z);
  }
}