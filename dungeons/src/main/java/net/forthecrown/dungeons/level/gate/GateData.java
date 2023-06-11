package net.forthecrown.dungeons.level.gate;

import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.math.Direction;
import org.spongepowered.math.vector.Vector3i;

public record GateData(Direction direction,
                       Vector3i parentOffset,
                       boolean stairs,
                       Opening opening
) {

  public static final Opening DEFAULT_OPENING = new Opening(12, 21);

  public static final String
      TAG_OPENING = "opening",
      TAG_CORRECT = "autocorrect_placement",
      TAG_WIDTH = "width",
      TAG_STAIR = "connects_to_stairs",
      TAG_HEIGHT = "height";

  public AbsoluteGateData toAbsolute(DungeonPiece parent) {
    var rot = parent.getRotation();

    return new AbsoluteGateData(
        direction.rotate(rot),
        parent.getPivotPosition().add(rot.rotate(parentOffset)),
        stairs,
        opening
    );
  }

  public record Opening(int width, int height) {

    public CompoundTag save() {
      CompoundTag tag = BinaryTags.compoundTag();
      tag.putInt(TAG_WIDTH, width);
      tag.putInt(TAG_HEIGHT, height);
      return tag;
    }

    public static Opening load(BinaryTag t) {
      if (!(t instanceof CompoundTag tag)) {
        return DEFAULT_OPENING;
      }

      if (!tag.contains(TAG_WIDTH) && !tag.contains(TAG_HEIGHT)) {
        return DEFAULT_OPENING;
      }

      int width = tag.getInt(TAG_WIDTH);
      int height = tag.getInt(TAG_HEIGHT);

      if (width == DEFAULT_OPENING.width() && height == DEFAULT_OPENING.height()) {
        return DEFAULT_OPENING;
      }

      return new Opening(width, height);
    }
  }
}