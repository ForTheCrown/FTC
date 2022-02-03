package net.forthecrown.structure;

import net.forthecrown.utils.math.MathUtil;
import net.minecraft.world.level.block.Rotation;

/**
 * The rotation of a build,
 * used when placing the build
 */
public enum PlaceRotation {
    D_0 (0),
    D_90 (90),
    D_180 (180),
    D_270 (270);

    final int degrees;

    PlaceRotation(int d) {
        this.degrees = d;
    }

    public int getDegrees() {
        return degrees;
    }

    public PlaceRotation add(PlaceRotation other) {
        return add0(other.ordinal());
    }

    public PlaceRotation subtract(PlaceRotation other) {
        return add0(-other.ordinal());
    }

    private PlaceRotation add0(int add) {
        int newOrdinal = ordinal() + add;
        PlaceRotation[] values = values();

        if(MathUtil.isInRange(add, 0, values.length - 1)) {
            return values[newOrdinal];
        }

        if(newOrdinal < 0) return values[newOrdinal + values.length];
        else return values[newOrdinal - values.length];
    }

    public Rotation toVanilla() {
        return switch (this) {
            case D_0 -> Rotation.NONE;
            case D_90 -> Rotation.CLOCKWISE_90;
            case D_180 -> Rotation.CLOCKWISE_180;
            case D_270 -> Rotation.COUNTERCLOCKWISE_90;
        };
    }
}
