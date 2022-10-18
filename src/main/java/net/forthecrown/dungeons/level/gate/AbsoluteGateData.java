package net.forthecrown.dungeons.level.gate;

import net.forthecrown.structure.Direction;
import net.forthecrown.structure.Rotation;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.math.vector.Vector3i;

import static net.forthecrown.dungeons.level.gate.GateData.TAG_OPENING;

public record AbsoluteGateData(Direction direction, Vector3i center, GateData.Opening opening) {
    private static final String
            TAG_DIRECTION = "direction",
            TAG_CENTER = "center";

    public Vector3i rightSide() {
        var right = direction.right();
        var halfWidth = opening.width() / 2;

        return center.add(right.getMod().mul(halfWidth, 0, halfWidth))
                .sub(direction.getMod());
    }

    public AbsoluteGateData apply(Transform transform) {
        var dir = direction;

        if (transform.getRotation() != Rotation.NONE) {
            dir = direction.rotate(transform.getRotation());
        }

        return new AbsoluteGateData(dir, transform.apply(center), opening);
    }

    public CompoundTag save() {
        CompoundTag result = new CompoundTag();
        result.put(TAG_DIRECTION, TagUtil.writeEnum(direction));
        result.put(TAG_CENTER, Vectors.writeTag(center));
        result.put(TAG_OPENING, opening.save());
        return result;
    }

    public static AbsoluteGateData load(Tag t) {
        if (!(t instanceof CompoundTag tag)) {
            return null;
        }

        Direction dir = TagUtil.readEnum(Direction.class, tag.get(TAG_DIRECTION));
        Vector3i center = Vectors.read3i(tag.get(TAG_CENTER));
        GateData.Opening opening = GateData.Opening.load(tag.get(TAG_OPENING));

        return new AbsoluteGateData(dir, center, opening);
    }
}