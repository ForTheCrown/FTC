package net.forthecrown.structure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.util.Index;
import org.apache.commons.lang3.Validate;
import org.bukkit.block.BlockFace;
import org.spongepowered.math.vector.Vector3i;

@Getter
@RequiredArgsConstructor
public enum Direction {
    // NAME,                  OFFSET,   AXIS, LEFT, RIGHT, OPPOSITE
    UP    (Vector3i.from( 0,  1,  0), Axis.Y,   -1,    -1,        1),
    DOWN  (Vector3i.from( 0, -1,  0), Axis.Y,   -1,    -1,        0),
    NORTH (Vector3i.from( 0,  0, -1), Axis.Z,    5,     4,        3),
    SOUTH (Vector3i.from( 0,  0,  1), Axis.Z,    4,     5,        2),
    EAST  (Vector3i.from( 1,  0,  0), Axis.X,    2,     3,        5),
    WEST  (Vector3i.from(-1,  0,  0), Axis.X,    3,     2,        4);

    private final Vector3i mod;
    private final Axis axis;
    private final int leftOrdinal;
    private final int rightOrdinal;
    private final int oppositeOrdinal;

    private static final Direction[] VALUES = values();
    private static final Index<String, Direction> BY_NAME = Index.create(Direction::name, VALUES);

    public Direction opposite() {
        return VALUES[oppositeOrdinal];
    }

    public Direction left() {
        return byOrdinal(leftOrdinal);
    }

    public Direction right() {
        return byOrdinal(rightOrdinal);
    }

    private static Direction byOrdinal(int o) {
        return o == -1 ? null : VALUES[o];
    }

    public boolean isRotatable() {
        return this != UP && this != DOWN;
    }

    public Direction rotate(Rotation rotation) throws IllegalArgumentException {
        validateRotatable();

        return switch (rotation) {
            case NONE -> this;
            case COUNTERCLOCKWISE_90 -> left();
            case CLOCKWISE_90 -> right();
            case CLOCKWISE_180 -> opposite();
        };
    }

    /**
     * Derives the rotation needed to get from
     * this direction to the other direction
     * @param other The direction to rotate towards
     * @return The derived rotation
     */
    public Rotation deriveRotationFrom(Direction other) throws IllegalArgumentException {
        validateRotatable();

        if (this == other) {
            return Rotation.NONE;
        } else if (left() == other) {
            return Rotation.COUNTERCLOCKWISE_90;
        } else if (right() == other) {
            return Rotation.CLOCKWISE_90;
        } else {
            return Rotation.CLOCKWISE_180;
        }
    }

    private void validateRotatable() {
        Validate.isTrue(isRotatable(), "Cannot rotate down or up facing direction");
    }

    public static Direction fromBukkit(BlockFace face) {
        Validate.isTrue(face.isCartesian(), "Non cartesian block face given");
        return BY_NAME.value(face.name());
    }

    public BlockFace asBlockFace() {
        return BlockFace.valueOf(name());
    }

    /* ----------------------------- SUB CLASSES ------------------------------ */

    public enum Axis {
        X,
        Y,
        Z;

        public double choose(double x, double y, double z) {
            return switch (this) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            };
        }

        public int choose(int x, int y, int z) {
            return switch (this) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            };
        }
    }
}