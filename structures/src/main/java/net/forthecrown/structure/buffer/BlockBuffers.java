package net.forthecrown.structure.buffer;

import net.forthecrown.utils.math.Bounds3i;
import org.bukkit.World;

public class BlockBuffers {

  public static BlockBuffer immediate(World world) {
    return new ImmediateBlockBuffer(world);
  }

  public static BlockBuffer allocate(Bounds3i area) {
    return new ChunkedBlockBuffer(area);
  }
}