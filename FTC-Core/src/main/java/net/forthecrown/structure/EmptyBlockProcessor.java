package net.forthecrown.structure;

import org.jetbrains.annotations.Nullable;

/**
 * A block processor which will either return the previous result or
 * unmodified place data, depending on if the previous result was
 * null or not
 */
public class EmptyBlockProcessor implements BlockProcessor {
    @Override
    public @Nullable BlockPlaceData process(BlockPalette palette, BlockPalette.StateData data, StructurePlaceContext context, @Nullable BlockPlaceData previousResult) {
        return previousResult == null ? new BlockPlaceData(context.toAbsolute(data.offset()), palette.getState(), data.tag()) : previousResult;
    }
}
