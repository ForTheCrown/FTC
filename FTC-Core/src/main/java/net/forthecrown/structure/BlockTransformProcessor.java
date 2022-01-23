package net.forthecrown.structure;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockTransformProcessor implements BlockProcessor {
    @Override
    public @Nullable BlockPlaceData process(BlockPalette palette, BlockPalette.StateData data, StructurePlaceContext context, @Nullable BlockPlaceData previousResult) {
        if(previousResult == null) return null;
        BlockState state = previousResult.state();

        state = state.rotate(context.getRotation().toVanilla());

        return new BlockPlaceData(previousResult.absolutePos(), state, previousResult.tag());
    }
}
