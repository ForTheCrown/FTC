package net.forthecrown.protection;

import org.apache.commons.lang3.ArrayUtils;

public final class ClaimUtil {
    private ClaimUtil() {}

    public static ClaimPos[] findApplicableSections(Bounds2i b) {
        ClaimPos[] result = {};
        Bounds2i s = b.toSectionBounds();

        for (int x = s.minX(); x <= s.maxX(); x++) {
            for (int z = s.minZ(); z <= s.maxZ(); z++) {
                ClaimPos p = new ClaimPos(x, z);
                if(ArrayUtils.contains(result, p)) continue;

                result = ArrayUtils.add(result, p);
            }
        }

        return result;
    }
}