package net.forthecrown.math;

public class Vec2i {
    public static final Vec2i ZERO = new Vec2i(0, 0);

    private final int x, z;

    public Vec2i(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public Vec2i add(int x, int z) {
        return new Vec2i(this.x + x, this.z + z);
    }

    public Vec2i add(Vec2i v) {
        return add(v.x, v.z);
    }

    public Vec2i subtract(int x, int z) {
        return new Vec2i(this.x - x, this.z - z);
    }

    public Vec2i subtract(Vec2i v) {
        return subtract(v.x, v.z);
    }

    public Vec2i min(Vec2i other) {
        return new Vec2i(
                Math.min(x, other.x),
                Math.min(z, other.z)
        );
    }

    public Vec2i max(Vec2i other) {
        return new Vec2i(
                Math.max(x, other.x),
                Math.max(z, other.z)
        );
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }
}
