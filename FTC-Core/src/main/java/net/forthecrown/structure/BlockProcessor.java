package net.forthecrown.structure;

import org.jetbrains.annotations.Nullable;

/**
 * Processes a block when placing a structure
 */
public interface BlockProcessor {
    /**
     * Processes the given block for placing
     * <p></p>
     * If the final result of all processors being run is null, then no block
     * will be placed, thus, if the context holds no processors, the structure won't be
     * placed
     *
     * @param palette The palette the block is in
     * @param data The specific state instance being processed
     * @param context The placement context
     * @param previousResult The result given by the previous processor that was ran for this block, can be null
     * @return The block placement data, null, if no block is to be placed
     */
    @Nullable BlockPlaceData process(BlockPalette palette, BlockPalette.StateData data, StructurePlaceContext context, @Nullable BlockPlaceData previousResult);
}
