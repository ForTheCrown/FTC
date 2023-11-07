package net.forthecrown.utils.math;

import java.util.Iterator;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.spongepowered.math.vector.Vector3i;

public interface AreaSelection extends Iterable<Block> {

  World getWorld();

  Vector3i min();

  Vector3i max();

  Vector3i size();

  Iterator<Entity> entities();
}
