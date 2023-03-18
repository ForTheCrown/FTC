package net.forthecrown.structure;

import com.mojang.serialization.Dynamic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.nbt.paper.TagTranslators;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.TagOps;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlockStates;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

@Getter
@RequiredArgsConstructor
public class BlockInfo {
  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * The NBT key of the NBT data field
   */
  public static final String TAG_NBT = "data";

  /**
   * The NBT key of the block data field
   */
  public static final String TAG_STATE = "state";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * The block data of this info
   */
  private final BlockData data;

  /**
   * The NBT data of this block's block entity, null, if this info's block is not a block entity
   */
  @Nullable
  private final CompoundTag tag;

  /* ----------------------------- METHODS ------------------------------ */

  /**
   * Gets the block state of this block info
   *
   * @return The block state
   */
  public BlockState getState(Vector3i pos) {
    CompoundTag tag = getTag();

    if (tag != null && tag.isEmpty()) {
      tag = null;
    }

    return CraftBlockStates.getBlockState(
        Vectors.toMinecraft(pos),
        VanillaAccess.getState(getData()),
        tag == null ? null : TagTranslators.COMPOUND.toMinecraft(tag)
    );
  }

  BlockInfo fixData(int oldVersion, int newVersion) {
    if (tag == null || tag.isEmpty()) {
      return this;
    }

    return withTag(
        (CompoundTag) DataFixers.getDataFixer()
            .update(
                References.BLOCK_ENTITY,
                new Dynamic<>(TagOps.OPS, tag),
                oldVersion, newVersion
            )
            .getValue()
    );
  }

  void place(StructurePlaceConfig config, Vector3i position) {
    Vector3i pos = config.getTransform().apply(position);
    var buf = config.getBuffer();
    buf.setBlock(pos, getData(), getTag());
  }

  public BlockData getData() {
    return data.clone();
  }

  static CompoundTag copyTag(CompoundTag tag) {
    return tag == null ? null : tag.copy();
  }

  /* ----------------------------- CLONE METHODS ------------------------------ */

  public BlockInfo withData(BlockData data) {
    return new BlockInfo(data.clone(), copyTag(tag));
  }

  public BlockInfo withTag(CompoundTag tag) {
    return new BlockInfo(data.clone(), copyTag(tag));
  }

  public BlockInfo withState(BlockState state) {
    CompoundTag tag = null;

    if (state instanceof TileState entityState) {
      tag = PaperNbt.saveBlockEntity(entityState);
    }

    return withTag(tag)
        .withData(state.getBlockData());
  }

  public BlockInfo copy() {
    return new BlockInfo(data.clone(), copyTag(tag));
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public static BlockInfo load(BinaryTag t) {
    CompoundTag tag = (CompoundTag) t;
    CompoundTag blockTag = null;

    if (tag.containsKey(TAG_NBT)) {
      blockTag = tag.get(TAG_NBT).asCompound();
    }

    return new BlockInfo(
        PaperNbt.loadBlockData(tag.get(TAG_STATE).asCompound()),
        blockTag
    );
  }

  public void save(CompoundTag tag) {
    tag.put(TAG_STATE, PaperNbt.saveBlockData(data));

    if (this.tag != null && !this.tag.isEmpty()) {
      tag.put(TAG_NBT, this.tag);
    }
  }

  /* ----------------------------------------------------------- */

  public static BlockInfo of(Block block) {
    CompoundTag tag = null;

    if (block.getState() instanceof TileState tile) {
      tag = PaperNbt.saveBlockEntity(tile);
    }

    return new BlockInfo(block.getBlockData(), tag);
  }

  /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof BlockInfo info)) {
      return false;
    }

    return new EqualsBuilder()
        .append(getData(), info.getData())
        .append(getTag(), info.getTag())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getData())
        .append(getTag())
        .toHashCode();
  }
}