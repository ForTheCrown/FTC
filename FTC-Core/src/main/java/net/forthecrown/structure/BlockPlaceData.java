package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The data used to place a block. when placing a structure
 */
public record BlockPlaceData(Vector3i absolutePos, BlockState state, CompoundTag tag) {
    public FtcBlockData toPlaceable() {
        return new FtcBlockData() {
            @Override
            public @NotNull BlockData getData() {
                return state.createCraftBlockData();
            }

            @Override
            public @Nullable CompoundTag getTag() {
                return tag;
            }
        };
    }
}
