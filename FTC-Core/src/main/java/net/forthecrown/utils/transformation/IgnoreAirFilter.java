package net.forthecrown.utils.transformation;

/**
 * Small filter for ignoring all copied air blocks
 */
public class IgnoreAirFilter implements BlockFilter {
    @Override
    public boolean test(BlockCopyInfo info, RegionCopyPaste paste, boolean async) {
        return !info.copy().getType().isAir();
    }
}
