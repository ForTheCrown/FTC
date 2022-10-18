package net.forthecrown.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A processor for a single block that's being placed by
 * a {@link BlockStructure}.
 */
@FunctionalInterface
public interface BlockProcessor {
    /**
     * Processes a single block.
     * <p>
     * The processors are ran as a list for every single block
     * that's placed, this means that the given block data will be
     * modified by a processor and then passed onto the next
     * processor in the list.
     * <p>
     * If the final result of all processors being ran is null, then
     * no block is placed, otherwise the returned block data will be
     * placed.
     *
     * @param original The original block info of the block being placed
     * @param previous The result of the previous processor, will be
     *                 original if there was no processor before this
     *                 one
     * @param config The placement config
     *
     * @return The processed block info, null, for no block placement
     */
    @Nullable BlockInfo process(@NotNull  BlockInfo original,
                                @Nullable BlockInfo previous,
                                @NotNull  StructurePlaceConfig config
    );
}