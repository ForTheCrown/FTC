package net.forthecrown.structure;

import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Random;

public enum Rotation {
    /*   0 degrees */ NONE,
    /*  90 degrees */ CLOCKWISE_90,
    /* 180 degrees */ CLOCKWISE_180,
    /* 270 degrees */ COUNTERCLOCKWISE_90;

    private static final Rotation[] VALUES = values();

    public static Rotation random(Random random) {
        return VALUES[random.nextInt(VALUES.length)];
    }

    public Rotation add() {
        return add(CLOCKWISE_90);
    }

    public Rotation add(Rotation other) {
        return VALUES[(other.ordinal() + ordinal()) % VALUES.length];
    }

    public Vector3i rotate(Vector3i v) {
        return rotate(v.x(), v.y(), v.z());
    }

    public Vector3i rotate(int x, int y, int z) {
        return switch (this) {
            case NONE                -> Vector3i.from( x,  y,  z);
            case CLOCKWISE_90        -> Vector3i.from(-z,  y,  x);
            case CLOCKWISE_180       -> Vector3i.from(-x,  y, -z);
            case COUNTERCLOCKWISE_90 -> Vector3i.from( z,  y, -x);
        };
    }

    public Vector3d rotate(Vector3d v) {
        return rotate(v.x(), v.y(), v.z());
    }

    public Vector3d rotate(double x, double y, double z) {
        return switch (this) {
            case NONE                -> Vector3d.from( x,  y,  z);
            case CLOCKWISE_90        -> Vector3d.from(-z,  y,  x);
            case CLOCKWISE_180       -> Vector3d.from(-x,  y, -z);
            case COUNTERCLOCKWISE_90 -> Vector3d.from( z,  y, -x);
        };
    }
}