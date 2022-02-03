package net.forthecrown.protection;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.utils.math.ImmutableVector3i;

public record ClaimPos(int x, int z) {
    public static final int
            BIT_SHIFT       = 6,
            SECTION_SIZE    = (int) Math.pow(2, BIT_SHIFT);

    public ClaimPos add(int x, int z) {
        return new ClaimPos(this.x + x, this.z + z);
    }

    public static ClaimPos of(ImmutableVector3i v) {
       return ofAbsolute(v.getX(), v.getZ());
    }

    public static ClaimPos of(BlockVector2 v) {
        return ofAbsolute(v.getX(), v.getZ());
    }

    public static ClaimPos ofAbsolute(int x, int z) {
        return new ClaimPos(toSection(x), toSection(z));
    }

    public static int toSection(int abs) {
        return abs >> BIT_SHIFT;
    }

    public static int toAbsolute(int section) {
        return section << BIT_SHIFT;
    }
}
