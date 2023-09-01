package net.forthecrown.structure.buffer;

import java.util.concurrent.CompletableFuture;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Transform;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

public interface BlockBuffer {

  default CompletableFuture<Void> place(World world) {
    return place(world, Transform.IDENTITY);
  }

  default CompletableFuture<Void> place(World world, Transform transform) {
    return place(world, transform, false);
  }

  CompletableFuture<Void> place(World world, Transform transform, boolean updatePhysics);

  default BufferBlock getBlock(Vector3i pos) {
    return getBlock(pos.x(), pos.y(), pos.z());
  }

  @Nullable BufferBlock getBlock(int x, int y, int z);

  default void setBlock(Vector3i pos, BlockData data, CompoundTag tag) {
    setBlock(pos.x(), pos.y(), pos.z(), data, tag);
  }

  default void setBlock(int x, int y, int z, BlockData data, CompoundTag tag) {
    setBlock(x, y, z, new BufferBlock(data, tag));
  }

  default void setBlock(Vector3i pos, BufferBlock block) {
    setBlock(pos.x(), pos.y(), pos.z(), block);
  }

  void setBlock(int x, int y, int z, @Nullable BufferBlock block);

  @Nullable Bounds3i getBounds();

  default boolean isBoundaryLimited() {
    return getBounds() == null;
  }

}