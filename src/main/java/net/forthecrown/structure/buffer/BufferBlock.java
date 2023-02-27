package net.forthecrown.structure.buffer;

import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;

public record BufferBlock(BlockData data, CompoundTag tag) {

  static BufferBlock fromBlock(Block b) {
    BlockData data = b.getBlockData();
    BlockState state = b.getState();

    if (state instanceof TileState tileState) {
      return new BufferBlock(data, PaperNbt.saveBlockEntity(tileState));
    } else {
      return new BufferBlock(data, null);
    }
  }

  @Override
  public CompoundTag tag() {
    return tag == null ? null : tag.copy();
  }

  public void apply(Block block, boolean update) {
    block.setBlockData(data, update);
    var state = block.getState();

    if (state instanceof TileState tile
        && tag != null
        && !tag.isEmpty()
    ) {
      PaperNbt.loadBlockEntity(tile, tag);
      tile.update(true, update);
    }
  }
}