package net.forthecrown.structure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.block.Rotation;
import org.dynmap.utils.Matrix3D;
import org.dynmap.utils.Vector3D;

/**
 * The rotation of a build,
 * used when placing the build
 */
@RequiredArgsConstructor
public enum PlaceRotation implements Transformer {
    D_0 (0),
    D_90 (90),
    D_180 (180),
    D_270 (270);

    @Getter
    final int degrees;

    public PlaceRotation add(PlaceRotation other) {
        return add0(other.ordinal());
    }

    public PlaceRotation subtract(PlaceRotation other) {
        return add0(-other.ordinal());
    }

    private PlaceRotation add0(int add) {
        int newOrdinal = ordinal() + add;
        PlaceRotation[] values = values();
        return values[newOrdinal % values.length];
    }

    public Rotation toVanilla() {
        return switch (this) {
            case D_0 -> Rotation.NONE;
            case D_90 -> Rotation.CLOCKWISE_90;
            case D_180 -> Rotation.CLOCKWISE_180;
            case D_270 -> Rotation.COUNTERCLOCKWISE_90;
        };
    }

    @Override
    public Vector3i transform(Vector3i pos) {
        double rotationRadians = Math.toRadians(getDegrees());
        double rotationSin = Math.sin(rotationRadians);
        double rotationCos = Math.cos(rotationRadians);
        Matrix3D rotationMatrix = new Matrix3D(rotationCos, 0.0D, -rotationSin, 0.0D, 1.0D, 0.0D, rotationSin, 0.0D, rotationCos);

        Vector3D vec = new Vector3D(pos.getX(), pos.getY(), pos.getZ());
        rotationMatrix.transform(vec);

        return Vector3i.of(vec.x, vec.y, vec.y);
    }
}