package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;
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
}
