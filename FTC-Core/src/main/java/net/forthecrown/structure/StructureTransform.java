package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface StructureTransform {
    StructureTransform DEFAULT = (start, offset, pivot, mirror, rotation) -> {
        int x = offset.getX();
        int y = offset.getY();
        int z = offset.getZ();
        boolean flag = true;

        switch (mirror) {
            case Z_AXIS -> z = -z;
            case X_AXIS -> x = -x;
            default -> flag = false;
        }

        int pivotX = pivot.getX();
        int pivotZ = pivot.getZ();

        Vector3i pos = switch (rotation) {
            case D_270 -> new Vector3i(pivotX - pivotZ + z, y, pivotX + pivotZ - x);
            case D_90 -> new Vector3i(pivotX + pivotZ - z, y, pivotZ - pivotX + x);
            case D_180 -> new Vector3i(pivotX + pivotX - x, y, pivotZ + pivotZ - z);
            default -> flag ? new Vector3i(x, y, z) : offset;
        };

        return pos.add(start);
    };

    @NotNull Vector3i transform(Vector3i start, Vector3i offset, Vector3i pivot, PlaceMirror mirror, PlaceRotation rotation);

    default @NotNull Vector3i transform(Vector3i start, Vector3i offset, PlaceRotation rotation) {
        return transform(start, offset, Vector3i.ZERO, PlaceMirror.NONE, rotation);
    }

    default @NotNull Vector3i transform(Vector3i offset, PlaceRotation rotation) {
        return transform(Vector3i.ZERO, offset, rotation);
    }

    default @NotNull BoundingBox transform(Vector3i start, Vector3i pivot, BoundingBox offset, PlaceRotation rotation) {
        Vector3i min = new Vector3i(offset.minY(), offset.minY(), offset.minZ());
        Vector3i max = new Vector3i(offset.maxX(), offset.maxY(), offset.maxZ());

        min = transform(start, min, rotation);
        max = transform(start, max, rotation);

        min = min.getMinimum(max);
        max = max.getMaximum(min);

        return new BoundingBox(
                min.getX(), min.getY(), min.getZ(),
                max.getX(), max.getY(), max.getZ()
        );
    }

    default @NotNull Vec3 transformDecimal(Vector3i start, Vec3 offset, Vector3i pivot, PlaceMirror mirror, PlaceRotation rotation) {
        // Hacky af approach of preserving the decimal
        // exactness of the Vec3 in an integer block
        // context

        // Get each cord's decimal numbers
        Vec3 decimalPlaces = new Vec3(
                offset.x - (long) offset.x,
                offset.y - (long) offset.y,
                offset.z - (long) offset.z
        );

        // Get the start block
        Vector3i offsetBlock = new Vector3i(
                (int) offset.x,
                (int) offset.y,
                (int) offset.z
        );

        // Transform block
        Vector3i transformed = transform(start, offsetBlock, pivot, mirror, rotation);

        // Re-add decimal places to block pos
        return decimalPlaces.add(
                transformed.getX(),
                transformed.getY(),
                transformed.getZ()
        );
    }
}
