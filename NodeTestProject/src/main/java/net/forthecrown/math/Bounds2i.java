package net.forthecrown.math;

public class Bounds2i {
    private final int
            minX, minZ,
            maxX, maxZ;

    public Bounds2i(Vec2i min, Vec2i max) {
        min = min.min(max);
        max = min.max(min);

        minX = min.getX();
        minZ = min.getZ();

        maxX = max.getX();
        maxZ = max.getZ();
    }

    public Vec2i max() {
        return new Vec2i(maxX, maxZ);
    }

    public Vec2i min() {
        return new Vec2i(minX, minZ);
    }

    public Vec2i min_max() {
        return new Vec2i(minX, maxZ);
    }

    public Vec2i max_min() {
        return new Vec2i(maxX, minZ);
    }

    public boolean overlaps(Bounds2i o) {
        return this.minX < o.maxX && this.maxX > o.minX
                && this.minZ < o.maxZ && this.maxZ > o.minZ;
    }

    public boolean contains(Vec2i v) {
        return contains(v.getX(), v.getZ());
    }

    public boolean contains(int x, int z) {
        return x >= this.minX && x < this.maxX
                && z >= this.minZ && z < this.maxZ;
    }
}
