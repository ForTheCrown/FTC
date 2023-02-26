package net.forthecrown.structure;

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

  CompletableFuture<Void> place(World world, Transform transform);

  default BufferedBlock getBlock(Vector3i pos) {
    return getBlock(pos.x(), pos.y(), pos.z());
  }

  BufferedBlock getBlock(int x, int y, int z);

  default void setBlock(Vector3i pos, BlockData data, CompoundTag tag) {
    setBlock(pos.x(), pos.y(), pos.z(), data, tag);
  }

  void setBlock(int x, int y, int z, BlockData data, CompoundTag tag);

  default void setBlock(Vector3i pos, BufferedBlock block) {
    setBlock(pos.x(), pos.y(), pos.z(), block);
  }

  void setBlock(int x, int y, int z, BufferedBlock block);

  @Nullable Bounds3i getBounds();

  default boolean isBoundaryLimited() {
    return getBounds() == null;
  }

  record BufferedBlock(BlockData data, CompoundTag tag) {

  }
}