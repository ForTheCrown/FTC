package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface StructureTransform {
    StructureTransform DEFAULT = (start, offset, pivot, mirror, rotation) -> {
        int i = offset.getX();
        int j = offset.getY();
        int k = offset.getZ();
        boolean flag = true;

        switch (mirror) {
            case LEFT_RIGHT -> k = -k;
            case FRONT_BACK -> i = -i;
            default -> flag = false;
        }

        int l = pivot.getX();
        int i1 = pivot.getZ();

        Vector3i pos = switch (rotation) {
            case D_270 -> new Vector3i(l - i1 + k, j, l + i1 - i);
            case D_90 -> new Vector3i(l + i1 - k, j, i1 - l + i);
            case D_180 -> new Vector3i(l + l - i, j, i1 + i1 - k);
            default -> flag ? new Vector3i(i, j, k) : offset;
        };

        return pos.add(start);
    };

    @NotNull Vector3i transform(Vector3i start, Vector3i offset, Vector3i pivot, PlaceMirror mirror, PlaceRotation rotation);

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
                transformed.x,
                transformed.y,
                transformed.z
        );
    }
}
