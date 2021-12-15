package net.forthecrown.utils.transformation;

/**
 * Transforms the copy and pasted block somehow
 * after it was copy and pasted, so any changes
 * made with the transformer will not be changed
 */
public interface CopyTransformer {
    void transform(BlockCopyInfo info, RegionCopyPaste paste, boolean async);
}
