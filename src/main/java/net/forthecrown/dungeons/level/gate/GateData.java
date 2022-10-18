package net.forthecrown.dungeons.level.gate;

import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.structure.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.math.vector.Vector3i;

public record GateData(Direction direction, Vector3i parentOffset, Opening opening) {
    public static final Opening DEFAULT_OPENING = new Opening(12, 21);

    public static final String
            TAG_OPENING = "opening",
            TAG_CORRECT = "autocorrect_placement",
            TAG_WIDTH = "width",
            TAG_HEIGHT = "height";

    public AbsoluteGateData toAbsolute(DungeonPiece parent) {
        var rot = parent.getRotation();

        return new AbsoluteGateData(
                direction.rotate(rot),
                parent.getPivotPosition().add(rot.rotate(parentOffset)),
                opening
        );
    }

    public record Opening(int width, int height) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_WIDTH, width);
            tag.putInt(TAG_HEIGHT, height);
            return tag;
        }

        public static Opening load(Tag t) {
            if (!(t instanceof CompoundTag)) {
                return DEFAULT_OPENING;
            }

            CompoundTag tag = (CompoundTag) t;

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