package net.forthecrown.math;

public interface Transform {
    Transform DEFAULT = (start, offset, pivot, mirror, rotation) -> {
        int x = offset.getX();
        int z = offset.getZ();
        boolean flag = true;

        switch (mirror) {
            case Z_AXIS -> z = -z;
            case X_AXIS -> x = -x;
            default -> flag = false;
        }

        int pivotX = pivot.getX();
        int pivotZ = pivot.getZ();

        Vec2i pos = switch (rotation) {
            case D_270 -> new Vec2i(pivotX - pivotZ + z, pivotX + pivotZ - x);
            case D_90 -> new Vec2i(pivotX + pivotZ - z, pivotZ - pivotX + x);
            case D_180 -> new Vec2i(pivotX + pivotX - x, pivotZ + pivotZ - z);
            default -> flag ? new Vec2i(x, z) : offset;
        };

        return pos.add(start);
    };

    Vec2i transform(Vec2i start, Vec2i offset, Vec2i pivot, Mirror mirror, Rot rotation);

    default Vec2i transform(Vec2i start, Vec2i offset, Rot rotation) {
        return transform(start, offset, Vec2i.ZERO, Mirror.NONE, rotation);
    }

    default Vec2i transform(Vec2i offset, Rot rotation) {
        return transform(Vec2i.ZERO, offset, rotation);
    }
}
