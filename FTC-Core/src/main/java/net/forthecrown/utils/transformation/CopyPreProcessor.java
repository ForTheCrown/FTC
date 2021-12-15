package net.forthecrown.utils.transformation;

/**
 * PreProcessor for the {@link RegionCopyPaste}.
 * Runs code before the block is copy and pasted so
 * the processor can change either the destination
 * or original block somehow.
 *
 * <p></p>
 * Any changes made to the destination block will
 * be overridden
 */
public interface CopyPreProcessor {
    void process(BlockCopyInfo block, RegionCopyPaste paste, boolean async);
}
