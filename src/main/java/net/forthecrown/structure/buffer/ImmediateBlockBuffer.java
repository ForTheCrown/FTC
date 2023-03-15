package net.forthecrown.structure.buffer;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Transform;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

@Getter
public class ImmediateBlockBuffer implements BlockBuffer {
  private final World world;

  ImmediateBlockBuffer(World world) {
    this.world = Objects.requireNonNull(world);
  }

  @Override
  public CompletableFuture<Void> place(World world,
                                       Transform transform,
                                       boolean updatePhysics
  ) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public BufferBlock getBlock(int x, int y, int z) {
    var block = world.getBlockAt(x, y, z);
    return BufferBlock.fromBlock(block);
  }

  @Override
  public void setBlock(int x, int y, int z, @Nullable BufferBlock block) {
    var b = world.getBlockAt(x, y, z);

    if (block == null) {
      b.setType(Material.AIR, false);
    } else {
      block.apply(b, false);
    }
  }

  @Override
  public @Nullable Bounds3i getBounds() {
    return null;
  }
}