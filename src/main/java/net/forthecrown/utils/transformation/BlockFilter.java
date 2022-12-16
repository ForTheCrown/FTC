package net.forthecrown.utils.transformation;

import org.bukkit.block.Block;

/**
 * A filter for the region copy and paste, determines
 * which blocks to ignore in the copy and paste
 */
@FunctionalInterface
public interface BlockFilter {
    boolean test(Block copy, Block paste, RegionCopyPaste copyPaste);
}