package net.forthecrown.protection;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.utils.math.ImmutableVector3i;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.bukkit.block.BlockFace;

public record Bounds2i(int minX, int minZ, int maxX, int maxZ) {

    public Bounds2i(int minX, int minZ, int maxX, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public boolean contains(int x, int z) {
        return x <= maxX && x >= minX
                && z <= maxZ && z >= minZ;
    }

    public boolean contains(BlockVector2 v) {
        return contains(v.getX(), v.getZ());
    }

    public boolean contains(ImmutableVector3i v) {
        return contains(v.getX(), v.getZ());
    }

    public int sizeX() {
        return maxX - minX;
    }

    public int sizeZ() {
        return maxZ - minZ;
    }

    public int area() {
        return sizeX() * sizeZ();
    }

    public BlockVector2 min() {
        return BlockVector2.at(minX, minZ);
    }

    public BlockVector2 max() {
        return BlockVector2.at(maxX, maxZ);
    }

    public BlockVector2 min_max() {
        return BlockVector2.at(minX, maxZ);
    }

    public BlockVector2 max_min() {
        return BlockVector2.at(maxX, minZ);
    }

    public boolean overlaps(Bounds2i o) {
        return contains(o.minX(), o.minZ())
                || contains(o.maxX(), o.maxZ())
                || contains(o.minX(), o.maxZ())
                || contains(o.maxX(), o.minZ());
    }

    public boolean contains(Bounds2i o) {
        return contains(o.minX(), o.minZ())
                && contains(o.maxX(), o.maxZ())
                && contains(o.minX(), o.maxZ())
                && contains(o.maxX(), o.minZ());
    }

    public Bounds2i toSectionBounds() {
        return new Bounds2i(
                ClaimPos.toSection(minX),
                ClaimPos.toSection(minZ),
                ClaimPos.toSection(maxX),
                ClaimPos.toSection(maxZ)
        );
    }

    public Bounds2i add(int size) {
        return new Bounds2i(
                minX - size,
                minZ - size,
                maxX + size,
                maxZ + size
        );
    }

    public Bounds2i add(int size, BlockFace face) {
        return new Bounds2i(
                minX - (face.getModX() * size),
                minZ - (face.getModZ() * size),
                maxX + (face.getModX() * size),
                maxZ + (face.getModZ() * size)
        );
    }

    public IntArrayTag save() {
        return new IntArrayTag(new int[] { minX, minZ, maxX, maxZ });
    }

    public static Bounds2i load(Tag tag) {
        IntArrayTag arr = (IntArrayTag) tag;
        int[] c = arr.getAsIntArray();

        return new Bounds2i(c[0], c[1], c[2], c[3]);
    }

    public static Bounds2i of(BlockVector2 min, BlockVector2 max) {
        return new Bounds2i(min.getX(), min.getZ(), max.getX(), max.getZ());
    }

    public BlockVector2[] corners() {
        return new BlockVector2[]{ min(), max(), max_min(), min_max() };
    }
}
