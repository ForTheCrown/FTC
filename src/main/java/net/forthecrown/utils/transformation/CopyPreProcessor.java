package net.forthecrown.utils.transformation;

import org.bukkit.block.Block;

/**
 * PreProcessor for the {@link RegionCopyPaste}.
 * Runs code before the block is copy and pasted so
 * the processor can change either the destination
 * or original block somehow.
 *
 * <p>
 * Any changes made to the destination block will
 * be overridden
 */
@FunctionalInterface
public interface CopyPreProcessor {
    void process(Block copy, Block paste, RegionCopyPaste copyPaste);
}