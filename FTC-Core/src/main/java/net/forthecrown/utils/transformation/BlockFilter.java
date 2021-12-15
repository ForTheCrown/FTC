package net.forthecrown.utils.transformation;

/**
 * A filter for the region copy and paste, determines
 * which blocks to ignore in the copy and paste
 */
public interface BlockFilter {
    boolean test(BlockCopyInfo info, RegionCopyPaste paste, boolean async);
}
