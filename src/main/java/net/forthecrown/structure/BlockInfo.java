package net.forthecrown.structure;

import com.mojang.serialization.Dynamic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockStates;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

@Getter
@RequiredArgsConstructor
public class BlockInfo {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /** The NBT key of the NBT data field */
    public static final String TAG_NBT = "data";

    /** The NBT key of the block data field */
    public static final String TAG_STATE = "state";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** The block data of this info */
    private final BlockData data;

    /**
     * The NBT data of this block's block entity, null,
     * if this info's block is not a block entity
     */
    @Nullable
    private final CompoundTag tag;

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Gets the block state of this block info
     * @return The block state
     */
    public BlockState getState(Vector3i pos) {
        CompoundTag tag = getTag();

        if (tag != null && tag.isEmpty()) {
            tag = null;
        }

        return CraftBlockStates.getBlockState(
                Vectors.toMinecraft(pos),
                VanillaAccess.getState(getData()),
                tag
        );
    }

    BlockInfo fixData(int oldVersion, int newVersion) {
        if (tag == null || tag.isEmpty()) {
            return this;
        }

        return withTag(
                (CompoundTag) DataFixers.getDataFixer()
                        .update(
                                References.BLOCK_ENTITY,
                                new Dynamic<>(NbtOps.INSTANCE, tag),
                                oldVersion, newVersion
                        )
                        .getValue()
        );
    }

    void place(StructurePlaceConfig config, Vector3i position) {
        Vector3i pos = config.getTransform().apply(position);

        var block = Vectors.getBlock(pos, config.getWorld());
        block.setBlockData(getData().clone(), false);

        if (tag != null && !tag.isEmpty()) {
            BlockEntity entity = VanillaAccess.getBlockEntity((TileState) block.getState());
            entity.load(tag.copy());
        }
    }

    /* ----------------------------- CLONE METHODS ------------------------------ */

    public BlockInfo withData(BlockData data) {
        return new BlockInfo(data.clone(), tag == null ? null : tag.copy());
    }

    public BlockInfo withTag(CompoundTag tag) {
        return new BlockInfo(data.clone(), tag == null ? null : tag.copy());
    }

    public BlockInfo withState(BlockState state) {
        CompoundTag tag = null;

        if (state instanceof CraftBlockEntityState<?> entityState) {
            tag = entityState.getTileEntity().saveWithoutMetadata();
        }

        return withTag(tag)
                .withData(state.getBlockData());
    }

    public BlockInfo copy() {
        return new BlockInfo(data.clone(), tag == null ? null : tag.copy());
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public static BlockInfo load(Tag t) {
        CompoundTag tag = (CompoundTag) t;
        CompoundTag blockTag = null;

        if (tag.contains(TAG_NBT)) {
            blockTag = tag.getCompound(TAG_NBT);
        }

        return new BlockInfo(
                TagUtil.readBlockData(tag.get(TAG_STATE)),
                blockTag
        );
    }

    public void save(CompoundTag tag) {
        tag.put(TAG_STATE, TagUtil.writeBlockData(data));

        if (this.tag != null && !this.tag.isEmpty()) {
            tag.put(TAG_NBT, this.tag);
        }
    }

    /* ----------------------------------------------------------- */

    public static BlockInfo of(Block block) {
        CompoundTag tag = null;

        if (block.getState() instanceof TileState tile) {
            tag = VanillaAccess.getBlockEntity(tile).saveWithoutMetadata();
        }

        return new BlockInfo(block.getBlockData(), tag);
    }

    /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BlockInfo)) {
            return false;
        }

        BlockInfo info = (BlockInfo) o;

        return new EqualsBuilder()
                .append(getData(), info.getData())
                .append(getTag(), info.getTag())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getData())
                .append(getTag())
                .toHashCode();
    }
}