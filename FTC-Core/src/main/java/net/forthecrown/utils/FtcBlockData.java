package net.forthecrown.utils;

import net.forthecrown.grenadier.types.block.ParsedBlock;
import net.forthecrown.royalgrenadier.types.block.ParsedBlockImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Bukkit's BlockData only contains data for a block's state,
 * aka the block's properties, but no NBT data.
 * Which is what this class is meant to help with
 */
public interface FtcBlockData {
    /**
     * Gets the data's Bukkit data
     * @return The bukkit data
     */
    @NotNull BlockData getData();

    /**
     * Gets the NBT data the block holds
     * @return The block's NBT data;
     */
    @Nullable CompoundTag getTag();

    /**
     * Creates the block data off of the block data,
     * <p></p>
     * Note: This will hold no NBT data
     * @param data The data to create from
     * @return The expanded data
     */
    static FtcBlockData of(BlockData data) {
        return new FtcBlockData() {
            @Override
            public @NotNull BlockData getData() {
                return data;
            }

            @Override
            public CompoundTag getTag() {
                return null;
            }
        };
    }

    /**
     * Creates the block data from the given tile entity
     * <p></p>
     * Will have the NBT data of the given tile entity
     * @param state The tile entity
     * @return The created data
     */
    static FtcBlockData of(TileState state) {
        BlockEntity entity = Bukkit2NMS.getBlockEntity(state);
        BlockState nnmsState = entity.getBlockState();
        CompoundTag data = entity.saveWithoutMetadata();

        return new FtcBlockData() {
            @Override
            public @NotNull BlockData getData() {
                return nnmsState.createCraftBlockData();
            }

            @Override
            public CompoundTag getTag() {
                return data;
            }
        };
    }

    /**
     * Just a thing to ensure that parsed blocks have all
     * their data correctly stored and/or placed
     * @param block The block
     * @return The created data
     */
    static FtcBlockData of(ParsedBlock block) {
        ParsedBlockImpl impl = (ParsedBlockImpl) block;

        return new FtcBlockData() {
            @Override
            public @NotNull BlockData getData() {
                return impl.getData();
            }

            @Override
            public @Nullable CompoundTag getTag() {
                return impl.getTags();
            }
        };
    }
}
