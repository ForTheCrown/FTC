package net.forthecrown.structure;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.serializer.NbtSerializable;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Represents a single state's instance in a block structure.
 * StateData contains every position and nbt tag for this instance
 * of the state within a structure.
 */
public class BlockPalette implements NbtSerializable {
    private final BlockState state;
    private final List<StateData> stateData = new ObjectArrayList<>();

    public BlockPalette(BlockState state ) {
        this.state = state;
    }

    public BlockPalette(CompoundTag tag) {
        this.state = NbtUtils.readBlockState(tag);
        ListTag list = tag.getList("stateData", Tag.TAG_COMPOUND);

        for (Tag t: list) {
            CompoundTag stateDataTag = (CompoundTag) t;

            BlockPos pos = NbtUtils.readBlockPos(stateDataTag);
            stateDataTag.remove("X");
            stateDataTag.remove("Y");
            stateDataTag.remove("Z");

            add(Vector3i.of(pos), stateDataTag.isEmpty() ? null : stateDataTag);
        }
    }

    public BlockState getState() {
        return state;
    }

    public List<StateData> getStateData() {
        return stateData;
    }

    public void add(Vector3i offset, CompoundTag tag) {
        stateData.add(new StateData(offset, tag));
    }

    /**
     * Represents a state's instance within a structure and it's
     * NBT data.
     * <p></p>
     * The vector stored is an offset from the minimum point
     * of the structure's bounding box
     */
    public record StateData(Vector3i offset, CompoundTag tag) {}

    @Override
    public CompoundTag save() {
        CompoundTag tag = NbtUtils.writeBlockState(state);

        ListTag list = new ListTag();

        for (StateData d: stateData) {
            // Serialize everything into one compound to save a bit of data
            CompoundTag data = d.tag() == null ? new CompoundTag() : d.tag();
            data.putInt("X", d.offset().getX());
            data.putInt("Y", d.offset().getY());
            data.putInt("Z", d.offset().getZ());

            list.add(data);
        }

        tag.put("stateData", list);
        return tag;
    }
}
